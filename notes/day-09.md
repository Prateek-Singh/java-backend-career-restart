# Day 9 - Duplicate Handling, Transaction Boundaries, Idempotency, Sliding Window, and SQL Aggregation

## Main Backend Milestone

Implemented explicit duplicate transaction handling across the service, persistence, and REST layers.

### Duplicate Transaction Behavior

Chosen API contract:

- A duplicate business transaction ID is treated as an error.
- REST returns `409 Conflict`.
- The error message identifies the duplicate transaction ID.
- The existing transaction is not returned as a successful create response.

Added a domain-specific `DuplicateTransactionException`.

The service performs an early duplicate check before persistence. This provides a clean failure path for normal duplicate requests.

Important concurrency limitation:

- An application-level `findById()` / existence check is not concurrency-safe.
- Two concurrent requests can both observe that the transaction does not yet exist.
- The database unique constraint remains the final race-safe guarantee.

### Database Unique-Constraint Handling

The Flyway-managed `transactions` table already enforces a unique constraint on the business transaction ID:

```text
uk_transactions_transaction_id
```

Updated the JPA persistence path so database-level duplicate violations are translated into `DuplicateTransactionException`.

Used `saveAndFlush()` in the JPA adapter so the insert is flushed while the adapter can still classify the persistence exception.

Important distinction:

```text
saveAndFlush()
-> sends pending SQL to the database

@Transactional service boundary
-> still controls final commit/rollback
```

A flush is not the same as a commit.

Only the expected transaction-ID unique constraint is classified as a duplicate. Other integrity violations are allowed to propagate rather than being incorrectly mapped to `409 Conflict`.

H2 exposed the named unique constraint using a decorated backing-index name, so constraint classification tolerates the H2 naming suffix while still checking for the intended constraint.

### H2 / Flyway Schema Validation

Changed the JPA test configuration to use:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

instead of Hibernate `create-drop`.

This keeps Flyway as the schema owner during integration testing and allows Hibernate to validate entity/schema compatibility.

Configured Hibernate's preferred UUID JDBC type as binary for the H2-backed Flyway schema so the Java UUID mapping aligns with the migration-defined `BINARY(16)` column.

### REST Duplicate Response

Added exception handling for `DuplicateTransactionException`.

Duplicate create requests now return:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Transaction with id TXN-100 already exists"
}
```

Added API integration coverage that:

1. creates the transaction successfully with `201 Created`
2. sends the same transaction again
3. verifies `409 Conflict`
4. verifies the structured error response

### Testing

Added/updated tests for:

- service-level duplicate detection
- no repository save after a known duplicate is detected
- duplicate exception handling
- JPA unique-constraint violation translation
- REST duplicate transaction response

The JPA integration test verifies that the first transaction is persisted and a second transaction with the same business ID is rejected.

Full Maven test suite passed after the changes.

## Transaction Boundaries

Added Spring's:

```java
org.springframework.transaction.annotation.Transactional
```

at the service/use-case layer.

The create-transaction use case uses:

```java
@Transactional
```

The transaction boundary belongs at the service level because the service represents the complete business unit of work. If the use case later contains multiple database writes, those operations can participate in the same transaction and commit or roll back together.

Read operations use:

```java
@Transactional(readOnly = true)
```

where appropriate.

Key learning:

- `readOnly = true` still participates in a transaction.
- It communicates read intent and may allow persistence-layer optimizations.
- It should not be treated as a security mechanism that guarantees writes are impossible.

Used Spring's `@Transactional` rather than Jakarta's annotation because Spring's version supports attributes such as `readOnly`, propagation, isolation, and rollback configuration.

Default Spring rollback behavior reinforced:

- unchecked `RuntimeException` -> rollback by default
- `Error` -> rollback by default
- checked exceptions require explicit rollback configuration when needed

Important concurrency point:

`@Transactional` does not eliminate the race between an existence check and an insert. The database unique constraint is still required.

## System Design - Retries, Idempotency, and Acknowledgement

Reviewed client retry behavior.

Scenario:

```text
POST transaction
-> database commit succeeds
-> HTTP response is lost
-> client retries same transaction ID
```

With the current API contract:

```text
retry
-> duplicate detected
-> 409 Conflict
-> no second database record
```

The business state remains duplicate-safe.

Discussed that another API design could store/replay the original response using a dedicated idempotency-key mechanism, but the current project intentionally uses duplicate `409` behavior.

### Async Consumer Scenario

Reviewed the equivalent asynchronous failure:

```text
consume message
-> persist successfully
-> crash before acknowledgement
-> message is redelivered
```

A confirmed duplicate business transaction should be treated as already processed and acknowledged rather than retried forever.

Failure classification:

```text
known duplicate
-> acknowledge / commit offset

transient database failure
-> retry with backoff

repeated unrecoverable failure
-> recovery / DLQ strategy
```

Acknowledging before database persistence is dangerous because:

```text
acknowledge
-> database write fails
-> broker considers message complete
-> transaction can be permanently lost
```

Preferred ordering:

```text
consume
-> process
-> database commit succeeds
-> acknowledge
```

A duplicate caused by crash-after-commit is preferable to message loss because the consumer can be made idempotent.

## DSA - Longest Substring Without Repeating Characters

Implemented the sliding-window solution using:

- `left` pointer
- `right` pointer
- `HashSet<Character>`
- current unique window
- `bestStart`
- `maxLength`

Core invariant:

> The active sliding window contains no duplicate characters.

When the current character already exists:

```java
while (set.contains(current)) {
    set.remove(input.charAt(left));
    left++;
}
```

Then add the current character and calculate:

```java
int currentLength = right - left + 1;
```

If the current window is the largest seen:

```java
if (currentLength > maxLength) {
    maxLength = currentLength;
    bestStart = left;
}
```

Return:

```java
input.substring(bestStart, bestStart + maxLength);
```

Complexity:

- Time: `O(n)`
- Space: `O(k)`, where `k` is the number of distinct characters in the active window

Added parameterized tests covering:

- `"abba"` -> `"ab"`
- `"abcabcbb"` -> `"abc"`
- `"bbbbb"` -> `"b"`
- `"pwwkew"` -> `"wke"`
- empty input
- null input

Key learning:

`left` represents the start of the current valid window, while `bestStart` remembers the start of the best window found so far.

## SQL - Conditional Aggregation Reinforcement

Practised per-account conditional aggregation.

Required output:

- total transaction count
- CREDIT count
- DEBIT count
- CREDIT amount
- DEBIT amount
- net amount
- only accounts with at least five transactions

Pattern:

```sql
SELECT
    account_id,
    COUNT(*) AS total_transaction_count,
    SUM(CASE WHEN transaction_type = 'CREDIT' THEN 1 ELSE 0 END)
        AS total_credit_transaction_count,
    SUM(CASE WHEN transaction_type = 'DEBIT' THEN 1 ELSE 0 END)
        AS total_debit_transaction_count,
    SUM(CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE 0 END)
        AS total_credit_amount,
    SUM(CASE WHEN transaction_type = 'DEBIT' THEN amount ELSE 0 END)
        AS total_debit_amount,
    SUM(CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE 0 END)
        -
    SUM(CASE WHEN transaction_type = 'DEBIT' THEN amount ELSE 0 END)
        AS net_amount
FROM transactions
GROUP BY account_id
HAVING COUNT(*) >= 5;
```

Reinforced:

```sql
COUNT(CASE WHEN condition THEN 1 ELSE 0 END)
```

is wrong for conditional counting because both `1` and `0` are non-null.

Correct alternatives:

```sql
SUM(CASE WHEN condition THEN 1 ELSE 0 END)
```

or:

```sql
COUNT(CASE WHEN condition THEN 1 END)
```

## Interview Communication

Practised explaining an idempotent transaction-creation API.

Key answer:

- use a stable business/idempotency key such as transaction ID
- perform an early service duplicate check for clean application behavior
- do not rely on that check for concurrency correctness
- enforce a database unique constraint as the final duplicate guard
- translate the expected unique violation into a domain duplicate exception
- map that exception to REST `409 Conflict`
- use a service-level transaction boundary so the database unit of work commits or rolls back together

## Day 9 Test Status

```text
mvn clean test
```

Passed.

## Day 9 Git Status

Commit and push pending at the time this note was written.
