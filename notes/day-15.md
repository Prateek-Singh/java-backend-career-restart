# Day 15 — Kafka Retry/DLT, Concurrency Reinforcement, 3Sum, SQL, System Design

## Closure Status

- Final `mvn clean test` passed.
- Kafka retry/backoff/DLT milestone is green.
- JaCoCo totals after Day 15:
    - instruction coverage: 82% (`627` missed of `3,564` instructions)
    - branch coverage: 77% (`85` missed of `380` branches)
    - line coverage: approximately 87.4% (`849` covered / `971` total)
    - method coverage: approximately 86.2% (`218` covered / `253` total)
    - class coverage: approximately 96.1% (`49` covered / `51` total)
- Focused study time and confidence are intentionally deferred until after commit/push, per the closure order.

## No-Notes Retrieval

Scores:

- `volatile` + `count++`: 8/10
- `synchronized` across multiple pods: 9/10
- optimistic vs pessimistic locking: 8.5/10
- Kafka crash after business commit but before offset progress: 8/10
- current idempotency concurrent-race limitation: 8/10
- RabbitMQ ACK vs Kafka committed offset: 8/10

Key reinforcement:

- `volatile` gives visibility/order guarantees, not atomicity for compound operations.
- `synchronized` coordinates threads sharing the same JVM monitor; it does not coordinate separate Spring Boot pods.
- database uniqueness/locking remains the shared cross-instance correctness mechanism.
- if business work commits but Kafka offset progress does not, redelivery can occur and the consumer must remain idempotent.

## Core Java — Concurrency Extension

### ReentrantLock — 7/10

Covered:

- explicit `lock()` / `unlock()` lifecycle
- `tryLock()`
- timed lock attempts
- `lockInterruptibly()`
- `Condition`
- `unlock()` in `finally`
- reentrancy is not unique to `ReentrantLock`; Java intrinsic locks are also reentrant

### Deadlocks — 8/10

Covered:

- circular lock ordering
- four Coffman conditions
- prevention through consistent global lock ordering
- keeping lock scope small
- avoiding unnecessary nested locks

### ConcurrentHashMap — 7/10

Covered:

- mostly non-blocking reads
- CAS/fine-grained internal synchronization concepts
- individual operations can be thread-safe while a multi-step compound sequence is not atomic
- use operations such as `putIfAbsent()` / `computeIfAbsent()` when the compound intent matters

### CompletableFuture — 7.5/10

Covered:

- `Future.get()` is blocking
- `thenApply()` transforms a completed value
- `thenCompose()` flattens dependent asynchronous stages
- `Async` variants may execute on another executor

## DSA — 3Sum

Score: 9/10

Pattern:

- sort
- fix one base element
- use left/right two pointers for the remaining target

Important corrections:

- skip duplicate base values
- skip duplicate left/right values after recording a match
- after a successful match, move both `left++` and `right--`; omitting these caused an initial infinite-loop bug

Tests covered:

- `[-1,0,1,2,-1,-4]`
- `[0,0,0]`
- `[0,0,0,0]`
- `[1,2,-2,-1]`
- empty input

Complexity:

- time: `O(n^2)`
- algorithmic extra space: `O(1)` excluding output and the sorting implementation details

## SQL — Latest Transaction Per Account

Score: 8.5/10

Used:

```sql
ROW_NUMBER() OVER (
    PARTITION BY account_id
    ORDER BY created_at DESC, transaction_id DESC
)
```

Key correction:

For MySQL last-30-day filtering use:

```sql
NOW() - INTERVAL 30 DAY
```

rather than PostgreSQL interval syntax.

## System Design — Transaction/Event Service at 500 RPS Growing 10x

Approximate score: 7/10

Covered:

- functional and reliability requirements
- `POST /transactions`
- `GET /transactions/{id}`
- `GET /accounts/{accountId}/transactions?page=...`
- `201`, `400`, `409`, `401`, `403`, `5xx`
- relational ACID persistence
- unique `transaction_id`
- `(account_id, created_at)` composite index
- transactional outbox design
- outbox metadata including event ID, aggregate ID, type, payload, status, retries, timestamps, and last error
- stateless application scaling
- DB connection-pool/write bottlenecks
- hot accounts/indexes
- Redis read caching
- Kafka partitions and consumer scaling
- outbox-publisher scaling
- Kafka outage behavior and backlog growth
- producer duplicate window after send-before-mark crash
- consumer redelivery after DB commit-before-offset crash
- JVM/API/DB/Redis/Kafka/outbox observability

Weakest area:

- capacity estimation and bottleneck diagnosis still needs more independent repetition.

## Spoken Kafka Answer

Topic: retryable vs non-retryable consumer failures

Score: approximately 7/10

Improved structure:

- transient DB/network/dependency timeout or outage -> bounded retry with backoff
- deterministic validation/malformed/unsupported-version failure -> do not repeatedly retry; recover to DLT
- exhausted transient failure -> DLT with sufficient metadata for diagnosis/replay

Recurring speaking gap:

- initial answer was too compressed; continue practising complete 60–90 second answers with classification, examples, policy, and recovery behavior.

## Kafka Retry / Backoff / DLT Implementation

### Event Validation

`TransactionCreatedEvent` now validates required event fields including:

- non-null `eventId`
- non-blank `eventType`
- non-null `eventTimestamp`
- non-blank `transactionId`
- non-blank `accountId`
- positive non-null `amount`
- non-null `transactionType`
- non-null `transactionCreatedAt`

The listener validates the payload before delegating to the business handler.

### Error Handling

Production consumer error handling now uses:

- `DefaultErrorHandler`
- `DeadLetterPublishingRecoverer`
- DLT topic: `transaction-events-dlt`
- same source partition number for DLT publication
- `ExponentialBackOffWithMaxRetries(3)`
- initial interval: 1 second
- multiplier: 2
- maximum interval: 4 seconds

Retry sequence:

```text
initial attempt
-> 1s retry
-> 2s retry
-> 4s retry
-> DLT after retries are exhausted
```

No custom DLT `KafkaTemplate` is used.

### Stable Test Strategy

The earlier approach of asserting exact handler/retry-listener invocation counts was removed because it was not a reliable integration-level contract across the full suite.

Final responsibility split:

1. Backoff policy unit test proves:
    - 1000 ms
    - 2000 ms
    - 4000 ms
    - STOP
2. Retry/DLT integration test proves:
    - retryable handler failure eventually reaches DLT
    - key is preserved
    - DLT value is non-empty
    - original-topic header exists
    - original-partition header exists
    - exception metadata exists
3. Validation/DLT integration path proves:
    - invalid event reaches DLT
    - business handler is not invoked
    - key/value are preserved

### Test Hardening

The Kafka integration test sets:

```properties
spring.kafka.consumer.properties.spring.json.use.type.headers=false
```

This prevents producer type headers from overriding the configured default consumer target type during tests.

DLT payloads are consumed as raw `byte[]` because representation can differ depending on the failure path. Tests deliberately do not deserialize the DLT payload back to `TransactionCreatedEvent` and do not assert a specific Spring wrapper exception class.

Unique event/transaction IDs are used per test to avoid stale-record interference.

### Known Follow-Up

Not yet claimed as complete:

- malformed JSON / raw-byte DLT serializer hardening
- integration proof for malformed JSON recovery
- graceful handling of the true concurrent processed-event uniqueness race
- transactional outbox implementation

## Applications / Career Track

Day 15/session application outcomes recorded for tracker update:

- Emeritus — Principal Engineer / Staff Software Engineer Java/Spring Boot — Applied
- Mastercard — Lead Software Engineer — Applied
- Barclays — Java Tech Lead — Applied, then Rejected
- SupplyHouse — Principal Backend Engineer — Applied, then Rejected
- Twilio — Principal Software Engineer India Remote — Applied
- JPMorganChase — Lead Software Engineer — Applied; HackerRank Assessment Received
- Morgan Stanley — Principal Software Engineer / VP, Risk Technology — Applied

JPMorganChase assessment information captured:

- duration shown: 60 minutes
- deadline: 1 Sep 2026, 2:16 AM EDT / 1 Sep 2026, 11:46 AM IST
- invite did not show number of questions, topics, or proctoring details

Exact application dates were not independently verified and should not be invented.

## Day 15 Technical Outcome

The main Day 15 Kafka milestone is complete:

```text
validated Kafka event
-> listener
-> retryable failure
-> bounded exponential retry
-> DLT recovery
```

with separate deterministic retry-policy proof and Testcontainers-based externally observable DLT behavior.

The full Maven test suite is green.
