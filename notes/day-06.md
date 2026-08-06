# Day 6

## Spring Boot Validation and Exception Handling

- Added Jakarta Bean Validation annotations to `CreateTransactionRequest`.
- Added `@Valid` at the controller boundary.
- Added structured `400 Bad Request` handling for `MethodArgumentNotValidException`.
- Added structured `400 Bad Request` handling for `HttpMessageNotReadableException`.
- Confirmed unsupported enum values fail during JSON deserialization.
- Added controller tests for blank IDs, blank account IDs, null and zero amounts, blank text fields, and unsupported enum values.
- Verified invalid HTTP requests do not call `TransactionService`.

## HTTP Validation and Business Invariants

- Bean Validation protects the HTTP request boundary.
- Service validation protects business rules for every caller, including non-HTTP callers.
- Current service-level invariants are:
    - transaction ID is required
    - account ID is required
    - amount must be positive
    - transaction type is required
- Description is currently validated at the HTTP boundary and is not yet treated as a service-level invariant.

## Transaction Type Domain Model

- Introduced the `TransactionType` enum:

```java
public enum TransactionType {
    CREDIT,
    DEBIT,
    TRANSFER,
    REFUND
}
```

- Migrated the request DTO, domain model, service, sample data, and tests from `String` values to `TransactionType`.
- Used the enum for type-safe domain representation.
- Invalid JSON values such as `"SALARY"` are rejected during deserialization and mapped to `400 Bad Request`.

## Repository and Integration Testing

- Added repository-backed transaction retrieval by account ID.
- Verified account-based retrieval without relying on result order.
- Added integration coverage for account-based transaction retrieval.
- Kept integration tests isolated by clearing in-memory repository state.
- Confirmed the complete Maven test suite passes:

```bash
mvn clean test
```

## DSA: K Closest Points to Origin

- Implemented K Closest Points to Origin using a size-limited max-heap.
- Calculated squared distance using `x² + y²`.
- Used `long` for the squared-distance calculation to reduce overflow risk.
- Avoided unnecessary square-root calculations.
- Kept at most `k` points in the heap.
- Removed the farthest retained point whenever the heap size exceeded `k`.
- Added tests for:
    - normal input
    - `k = 1`
    - `k` equal to the input size
    - duplicate points
    - invalid input
    - invalid `k`
    - order-independent output

### Complexity

```text
Time: O(n log k)
Auxiliary space: O(k)
```

## SQL Aggregation Practice

- Practised filtering rows with `WHERE`.
- Grouped transactions by account using `GROUP BY`.
- Calculated total amounts with `SUM`.
- Filtered grouped results using `HAVING`.
- Sorted aggregated results using `ORDER BY`.
- Limited the result set using `LIMIT`.
- Added a `CREDIT`-only filter.
- Reviewed the distinction:
    - `WHERE` filters rows before grouping.
    - `HAVING` filters groups after aggregation.

Example:

```sql
SELECT
    account_id,
    SUM(amount) AS total_amount
FROM transactions
WHERE amount IS NOT NULL
  AND type = 'CREDIT'
GROUP BY account_id
HAVING SUM(amount) > 10000
ORDER BY total_amount DESC
LIMIT 2;
```

## Repository Documentation

- Enhanced the repository-root `README.md`.
- Updated the endpoint, validation, testing, repository-structure, progress, and roadmap sections.
- Preserved the existing career-restart framing.
- Kept current project learning clearly separate from professional production experience.
