# Day 14 — Java Concurrency, Two Pointers, Persistent Kafka Idempotency, and System Design

## Focus

Day 14 focused on:
- Core Java concurrency and backend concurrency reasoning
- DSA transition from sliding window to two pointers
- persistent Kafka consumer idempotency
- real transactional rollback verification
- real Kafka-to-database integration
- first guided senior-style system-design walkthrough
- SQL and spoken interview practice

## 1. No-Notes Retrieval

Reviewed:
- Kafka committed offsets
- failure after business commit but before offset progress
- failure when offset progress occurs before business success
- atomic dedup + business mutation
- RabbitMQ exchange / queue / binding / routing key
- `volatile`, `synchronized`, and atomic classes
- `equals()` / `hashCode()`
- why JVM-local synchronization does not coordinate multiple pods

Main corrections:
- a committed Kafka offset represents consumer-group progress for a partition rather than simply “the last successfully processed event”
- `volatile` provides visibility/order guarantees; it does not make `count++` atomic
- `synchronized` provides mutual exclusion plus visibility for threads coordinating on the same monitor
- equal objects must have equal hash codes; equal hash codes do not imply equal objects

## 2. Java Concurrency

### Race condition and atomicity

Used a shared counter example:

```java
count++;
```

Reinforced that this is conceptually:

```text
read
-> modify
-> write
```

Two threads can read the same old value and overwrite one another, causing lost increments.

Key distinction:

```text
volatile
-> visibility / ordering

synchronized
-> mutual exclusion + visibility

AtomicInteger
-> atomic single-variable operations
```

`volatile` is appropriate for a simple shared flag such as:

```java
private volatile boolean running = true;
```

because readers need visibility of a single write. It is not sufficient for compound read-modify-write operations such as `count++`.

### JVM-local vs distributed concurrency

Reviewed:

```text
synchronized
-> coordinates threads inside one JVM

multiple Spring Boot pods
-> separate JVMs
-> separate monitors/locks
```

Therefore an application-level check such as:

```text
existsByTransactionId()
-> save()
```

is not race-safe across instances.

The database unique constraint remains the final cross-instance correctness guard for duplicate transaction IDs.

### Optimistic locking

Walkthrough:
- both pods read balance `1000`, version `7`
- Pod A debits `200`
- Pod A commits balance `800`, version `8`
- Pod B tries to update using stale version `7`
- optimistic-lock failure prevents a lost update
- Pod B may re-read the fresh state and retry/reject

### Pessimistic locking

Reviewed:
- database row is locked before mutation
- conflicting transaction waits
- provides stronger up-front coordination
- reduces concurrency and introduces blocking/deadlock risk

Default guidance:
- unique constraint for uniqueness
- optimistic locking when conflicts are uncommon
- pessimistic locking only when contention/criticality justifies it

### ExecutorService

Reviewed:
- thread reuse and controlled concurrency
- fixed pool of 2 allows at most two tasks to execute concurrently
- `shutdown()` stops accepting new tasks and lets submitted tasks finish
- `execute(Runnable)` returns nothing
- `submit(...)` accepts `Runnable`/`Callable` and returns a `Future`

## 3. DSA — Container With Most Water

Pattern:
- Two pointers

State:
- `left`
- `right`
- current `area`
- `maxArea`

Area:

```text
width  = right - left
height = min(height[left], height[right])
area   = width * height
```

Core invariant/reasoning:
- the shorter side limits the current container
- keeping the shorter side while reducing width cannot improve the area
- therefore move the pointer at the shorter height

Equal heights:
- moving either pointer is acceptable

Implementation complexity:
- Time: `O(n)`
- Extra space: `O(1)`

Tests:
- standard example -> 49
- two equal lines
- symmetric max-at-ends case
- empty array
- null array

Tests passed.

## 4. Persistent Kafka Consumer Idempotency Design

Goal:
prevent duplicate Kafka delivery from creating duplicate business effects.

Chosen persistence model:

```text
PROCESSED_EVENTS
- EVENT_ID primary key
- PROCESSED_AT
- CONSUMER_NAME

TRANSACTION_EVENT_AUDIT
- ID primary key
- EVENT_ID unique
- TRANSACTION_ID
- ACCOUNT_ID
- AMOUNT
- TRANSACTION_TYPE
- EVENT_TIMESTAMP
- CREATED_AT
```

Important modelling decision:
- `TRANSACTION_ID` is not a foreign key to the producer-side transactions table
- producer and consumer may be separate database/service boundaries
- `EVENT_ID` links the consumer-side dedup and audit records
- `TRANSACTION_ID` is not unique because one transaction may legitimately produce multiple different events over time

Database relationship:

```text
TRANSACTION_EVENT_AUDIT.EVENT_ID
-> PROCESSED_EVENTS.EVENT_ID
```

The JPA model deliberately keeps `eventId` as a scalar UUID instead of adding an unnecessary `@OneToOne` object relationship.

## 5. Flyway Migration

Added:

```text
V2__create_event_processing_audit_table.sql
```

The migration creates:
- `PROCESSED_EVENTS`
- `TRANSACTION_EVENT_AUDIT`
- primary/unique constraints
- consumer-side `EVENT_ID` foreign key
- transaction-ID lookup index

## 6. Kafka Persistence Package

Added logical Kafka-owned persistence instead of reusing transaction persistence:

```text
com.prateek.learning.kafka.persistence
├── entity
└── repository
```

Reason:
- transaction persistence owns the transaction domain/store
- processed-event and audit persistence belong to consumer event processing
- logical package ownership remains clean even while both currently use the same physical database

Added:
- `ProcessedEvent`
- `TransactionEventAudit`
- `ProcessedEventRepository`
- `TransactionEventAuditRepository`

## 7. TransactionEventProcessingService

Added:

```text
com.prateek.learning.kafka.processing.TransactionEventProcessingService
```

Processing flow:

```text
receive event
-> existsById(eventId)
-> if already processed: return
-> saveAndFlush ProcessedEvent
-> save TransactionEventAudit
-> commit
```

The method is `@Transactional`.

Semantic timestamps:
- `eventTimestamp` = when the event was created
- `processedAt` = when this consumer actually processed it
- `createdAt` = when the audit row was created

`saveAndFlush()` forces the processed-event uniqueness check to reach the database inside the service method.

Important limitation:
- the pre-check is not race-safe by itself
- DB primary/unique constraints remain the final guard
- graceful translation of a true concurrent uniqueness race into “already processed” is not yet implemented

## 8. Transactional Atomicity

Critical invariant:

```text
dedup record
+
audit/business effect
=
one successful processing decision
```

If:

```text
processed_events insert succeeds
audit insert fails
```

the entire local transaction must roll back.

Otherwise a redelivery could see the dedup marker and incorrectly skip business work that never committed.

## 9. Processing Service Tests

### Mockito tests

New event:
- verifies exact `eventId` existence check
- captures `ProcessedEvent`
- captures `TransactionEventAudit`
- verifies important mapped fields
- verifies both repository saves

Duplicate event:
- existence check returns true
- no processed-event save
- no audit save

### Spring/JPA integration tests

Successful processing:
- one processed-event row
- one audit row
- important persisted fields verified

Sequential duplicate processing:
- same event processed twice
- no exception on duplicate
- persistent counts remain 1 + 1

Rollback:
- audit record intentionally violates a NOT NULL database constraint
- service call raises `DataIntegrityViolationException`
- processed-event count returns to zero
- audit count remains zero
- event ID is not left marked as processed

This is direct proof of the `@Transactional` atomicity requirement.

## 10. Persistent Handler

Replaced the logging-only handler with:

```text
PersistentTransactionCreatedEventHandler
```

Current application path:

```text
TransactionCreatedEventConsumer
-> TransactionCreatedEventHandler
-> PersistentTransactionCreatedEventHandler
-> TransactionEventProcessingService
```

The logging-only handler was removed instead of using `@Primary`, avoiding multiple active beans for the same processing interface.

Added handler unit test:

```text
handler.handle(event)
-> processingService.process(event)
```

## 11. Real Kafka-to-Database Integration

Extended `TransactionEventFlowIntegrationTest`.

Real path:

```text
TransactionEventPublisher
-> KafkaTemplate
-> Testcontainers Kafka
-> JsonDeserializer
-> @KafkaListener
-> TransactionCreatedEventConsumer
-> PersistentTransactionCreatedEventHandler
-> TransactionEventProcessingService
-> PROCESSED_EVENTS
-> TRANSACTION_EVENT_AUDIT
```

### First-delivery test

Uses Awaitility to wait until:
- processed-event row exists
- audit row exists
- persisted transaction ID, account ID, amount, and transaction type match the event

### Duplicate-delivery test

Avoids an early-pass false positive:

1. publish event
2. wait until first delivery is fully persisted
3. publish same event again
4. use Awaitility `during(...)`
5. verify both table counts remain exactly one

This proves sequential duplicate Kafka redelivery does not repeat the persistent business/audit effect.

Known remaining hardening:
- simultaneous concurrent consumers passing `existsById(false)` is not yet covered by a concurrency integration test
- DB uniqueness remains the final race-safe guard
- graceful handling of the losing concurrent insert is future work

## 12. Guided System Design Walkthrough

First structured senior-style walkthrough:

```text
Requirements
-> non-functional requirements
-> API
-> data model
-> architecture
-> failure scenarios
```

### Requirements

System should:
- create transactions
- fetch by transaction ID
- fetch by account
- reject duplicate transaction IDs
- eventually publish a durable downstream event after successful transaction creation
- tolerate duplicate event delivery without duplicate business effects
- support approximately 500 RPS initially with room for higher peaks
- run across multiple application instances

### Consistency

Strong consistency:
- authoritative transaction creation
- uniqueness

Eventual consistency:
- downstream event processing

### API

Core endpoints:

```text
POST /transactions
GET /transactions/{transactionId}
GET /accounts/{accountId}/transactions
```

Discussed deriving `createdBy` from authenticated identity rather than blindly trusting a client-supplied field in a production design.

### Data/storage

MySQL chosen as source of truth because of:
- ACID
- transactions
- unique constraints
- integrity
- predictable account/time queries

Discussed:
- transaction table
- `(account_id, created_at)` style read index
- outbox table
- processed-event table

### Architecture

```text
Client
-> Load Balancer
-> Spring Boot instances
   -> MySQL
   -> Redis
   -> Outbox

Outbox Publisher
-> Kafka
-> Consumers
   -> processed_events
   -> consumer business tables
```

### Failure behavior

MySQL create failure:
- create cannot succeed because DB is source of truth
- appropriate 5xx/503 behavior
- cached reads may continue where valid
- sustained failures should alert

Kafka unavailable after DB + outbox commit:
- transaction remains durable
- outbox remains pending
- publisher retries after Kafka recovers
- monitor backlog/failure rate

Redis unavailable:
- fall back to MySQL
- correctness preserved
- latency/DB pressure increase
- short timeouts and alerts required

Consumer crash after business commit but before offset progress:
- Kafka can redeliver
- idempotent consumer prevents duplicate business effects

Duplicate Kafka event:
- stable `eventId`
- DB uniqueness
- atomic dedup + business mutation

Main interview improvement:
- describe system behavior and guarantee first
- describe operational alerting second

## 13. SQL

Problem:
return top 3 accounts by net transaction amount in the last 30 days.

Correct concepts:
- conditional CREDIT/DEBIT aggregation
- net amount
- date-range filter
- `GROUP BY`
- deterministic ordering

Corrections:
- requested tie-breaker was `account_id ASC`
- `LIMIT 3` was initially missed

SQL performance approximately 8/10.

## 14. Spoken Interview

Question:

“Why doesn’t `synchronized` solve duplicate requests across multiple Spring Boot pods?”

Core answer:
- `synchronized` coordinates threads sharing a monitor in one JVM
- each Spring Boot pod has its own JVM and monitor state
- requests hitting different pods can execute the synchronized method concurrently
- cross-instance correctness needs a shared coordination mechanism such as database uniqueness, transactional locking, or a justified distributed lock

Spoken performance approximately 8/10.

## 15. JaCoCo After Day 14

Final report:

- Instructions: 81% (`624` missed of `3379`)
- Branches: 76% (`82` missed of `354`)
- Lines: approximately 85.2% (`120` missed of `809`)
- Methods: approximately 83.4% (`35` missed of `211`)
- Classes: approximately 95.7% (`2` missed of `46`)

Compared with Day 13:
- instruction coverage remains 81%
- branch percentage moved from 77% to 76% as new branching behavior was added
- line coverage increased from approximately 84.5% to approximately 85.2%
- method coverage is approximately 83.4%
- class coverage remains high

Interpretation:
- the codebase grew with meaningful persistence and reliability behavior
- percentage movement should not be optimized artificially
- no hard coverage gate is introduced yet

## 16. Final Verification

Final command:

```bash
mvn clean test
```

Result:
- PASS

The Testcontainers-based Redis and Kafka tests remain self-contained with Docker available.

## Implemented After Day 14

Current hands-on project can now truthfully include:
- Kafka application consumer
- persistent consumer-side event deduplication
- stable `eventId` persistence
- atomic dedup + audit/business persistence
- rollback-safe consumer transaction
- sequential duplicate-redelivery suppression
- real Kafka-to-database integration testing

## Do Not Yet Claim as Implemented

- graceful handling/test of a true concurrent consumer uniqueness race
- Kafka retry/backoff
- Kafka DLT
- transactional outbox
- Kafka schema evolution/versioning
- production Kafka topology/replication
- JWT
- persisted user/account ownership authorization
- AWS deployment of the project

## Next Technical Direction

Recommended sequence:
1. short retrieval of Day 14 concurrency + Kafka idempotency
2. Kafka retry/backoff and DLT
3. transactional outbox
4. continue Core Java interview breadth
5. next DSA pattern reinforcement
6. daily system-design walkthrough with gradually less guidance
7. SQL + spoken interview
8. controlled applications / job-portal maintenance
