# Day 12 - Kafka Foundations, Messaging Reliability, DSA, System Design, and SQL

## Objectives

- Refresh Redis and Spring Security concepts without notes.
- Complete one DSA problem fully in-session.
- Learn Kafka fundamentals from first principles.
- Refresh RabbitMQ concepts and compare RabbitMQ with Kafka.
- Design and implement the first Kafka transaction-event producer milestone.
- Keep Kafka integration testing self-contained with Testcontainers.
- Practice system-design reasoning around database/broker consistency.
- Complete one SQL and spoken interview exercise.

## Retrieval Practice

### Redis

Recalled and reinforced:

- Cache hit: return the cached value and avoid the database.
- Cache miss: query MySQL, cache a successful result, and return it.
- Redis unavailable: the custom cache error handler allows fallback to MySQL.
- Redis outage can preserve correctness but still cause operational problems through higher latency and database load.
- Missing transactions are not negative-cached initially because a later create could otherwise leave a stale 404.
- Cache stampede: many requests miss/expire around the same time and fall through to the database.
- Hot key: one cache key receives disproportionately high traffic; expiry of a hot key can also trigger a stampede.

### Spring Security

Recalled and reinforced:

- 401: authentication is missing or invalid.
- 403: caller is authenticated but not allowed to perform the requested action.
- Stateless authentication means the server does not rely on a stored login session; each request carries enough authentication information.
- Current hands-on project authentication remains stateless HTTP Basic. JWT remains deliberately deferred.

## DSA - Max Consecutive Ones III

Problem:

Given a binary array and an integer `k`, flip at most `k` zeroes and return the maximum number of consecutive ones.

### Pattern

Sliding window with a zero counter.

### Why it fits

The required answer is the longest contiguous window that remains valid while containing at most `k` zeroes.

### State

- `left`
- `right`
- `zeroCount`
- `maxLength`

### Invariant

The active window must contain at most `k` zeroes.

### Invalid condition

`zeroCount > k`

### Repair

Move `left` forward until the window is valid again. Decrement `zeroCount` only when the outgoing value is zero.

### Answer update

After repairing the window:

`maxLength = max(maxLength, right - left + 1)`

### Important correction during walkthrough

When shrinking a window, removing a `1` does not reduce the zero count. The counter changes only when a zero leaves the window.

### Complexity

- Time: O(n)
- Space: O(1)

### Test coverage

Covered:

- standard examples,
- `k = 0`,
- all zeroes,
- enough flips to cover the entire array,
- empty array,
- null input.

The tests passed.

## Kafka Fundamentals

Kafka was learned from first principles because it was not previously part of the hands-on project.

### Topic

A named retained event log. Reading a Kafka record does not automatically remove it.

### Partition

A topic is split into partitions for scalability and parallelism.

Kafka ordering is guaranteed within a partition, not globally across the whole topic.

### Consumer group

Consumers in the same group cooperate to process partitions.

For a topic with three partitions, at most three consumers in the same group can actively consume those partitions at a time; additional consumers remain idle until a rebalance makes work available.

### Offset

A position within a partition. Consumer groups track their progress using offsets.

### Consumer lag

The difference between the latest available position and the consumer group's progress. Growing lag can indicate throughput or availability problems and can become serious if it threatens the retention window.

### Rebalance

When consumers join, leave, or fail, Kafka reassigns partitions among consumers in the group.

### Delivery and duplicate processing

A record may be processed successfully and then delivered again if the consumer crashes before its progress is committed. Therefore application consumers should be designed for idempotent processing.

### Retention and replay

Records remain available according to retention policy. Consumers can replay retained records by reading from older offsets.

## Ordering and Partition-Key Design

For `transaction-events`, the chosen Kafka key is:

`transactionId`

Reason:

- all events for the same transaction should map consistently to the same partition,
- ordering can therefore be preserved for that transaction,
- different transactions can still be processed in parallel.

Kafka does not provide global ordering across all partitions.

## Retry and Dead-Letter Concepts

For repeatedly failing records:

- use bounded retries,
- apply backoff,
- avoid infinite retry loops,
- after retry exhaustion, route the record to a dead-letter topic,
- preserve enough metadata for investigation and replay,
- monitor dead-letter growth.

Important distinction:

- idempotency protects against duplicate processing,
- an ordering strategy protects against applying later dependent events before an earlier failed event.

A poison record can also delay unrelated keys that happen to share the same partition, so retry and recovery policy must be designed carefully.

## RabbitMQ Refresher

Refreshed the RabbitMQ model:

`Producer -> Exchange -> Binding/Routing -> Queue -> Consumer`

Key concepts:

- Exchange: routing component receiving producer messages.
- Queue: stores messages waiting for consumers.
- Binding: connects an exchange to a queue using routing rules.
- Routing key: supplied with a published message and matched against binding rules.
- Consumer acknowledgement: confirms successful processing; the broker can then remove the message.
- DLX/DLQ: dead-letter exchange routes failed/dead-lettered messages to a dead-letter queue.

## RabbitMQ vs Kafka

### RabbitMQ

Prefer when the main requirement is traditional message-broker behavior:

- exchange and routing rules,
- queue-based task/command delivery,
- acknowledgements,
- competing consumers,
- flexible routing to queues.

### Kafka

Prefer when the main requirement is a durable event stream:

- retained records,
- replay,
- partitions and scalable parallel processing,
- multiple independent consumer groups,
- event-driven integration and analytics/audit use cases.

Do not choose solely by product popularity or raw throughput. Choose based on delivery model, retention/replay, routing, ordering requirements, and consumer independence.

## Kafka Project Design

### Topic

`transaction-events`

### First event

`TransactionCreated`

### Event contract

The event contains:

- `eventId`
- `eventType`
- `eventTimestamp`
- `transactionId`
- `accountId`
- `amount`
- `transactionType`
- `transactionCreatedAt`

`eventId` is separate from `transactionId`.

- `transactionId` identifies the business entity.
- `eventId` identifies one event occurrence and can later support deduplication.

### Delivery assumption

At-least-once.

### Consumer requirement

Future consumers should be idempotent.

## DB + Kafka Dual-Write Problem

Initial flow:

`validate -> save MySQL -> publish Kafka event -> return`

Failure scenario:

1. Database save succeeds.
2. Kafka publish fails.
3. The transaction exists in MySQL.
4. Downstream systems may never learn about the transaction.

This is a dual-write consistency problem.

### Transactional Outbox

Stronger future design:

1. In one local database transaction, insert:
    - the transaction row,
    - an outbox event row.
2. Commit both together.
3. A separate publisher reads unpublished outbox rows.
4. Publisher sends them to Kafka.
5. After success, mark the outbox record published.

Important remaining failure case:

- Kafka publish succeeds,
- publisher crashes before marking the outbox row published,
- the event is published again after restart.

Therefore transactional outbox still requires idempotent downstream processing. A stable `eventId` can support deduplication, and the consumer's deduplication record should ideally be committed in the same local transaction as its business update.

## Kafka Implementation

### Dependency

Added Spring Kafka and allowed Spring Boot dependency management to select the compatible version.

### Package structure

Created focused Kafka packages:

- `com.prateek.learning.kafka.event`
- `com.prateek.learning.kafka.producer`

### Event model

Added `TransactionCreatedEvent` as the Kafka event payload.

### Producer

Added `TransactionEventPublisher` using:

`KafkaTemplate<String, TransactionCreatedEvent>`

The publisher sends:

- topic: `transaction-events`
- key: `event.transactionId()`
- value: `TransactionCreatedEvent`

### Transaction service integration

After a successful repository save, `TransactionService` now:

1. builds a `TransactionCreatedEvent` from the saved transaction,
2. generates a separate event ID,
3. publishes the event,
4. returns the saved transaction.

This first milestone intentionally uses direct publishing and therefore retains the documented dual-write weakness. Transactional outbox is deferred as a later hardening step.

## Kafka Local Infrastructure

Added an Apache Kafka service to Docker Compose.

Created the topic explicitly:

- topic: `transaction-events`
- partitions: 3
- replication factor: 1

Three partitions are enough to demonstrate partitioning and consumer-group parallelism locally.

Replication factor 1 is appropriate only for the current single-broker learning environment, not a production high-availability design.

## Unit Tests

### TransactionEventPublisherTest

Mocks:

`KafkaTemplate<String, TransactionCreatedEvent>`

Verifies the publisher calls KafkaTemplate with:

- `transaction-events`,
- transaction ID as the key,
- the event as the payload.

### TransactionServiceTest

Extended the successful-create test to capture the published `TransactionCreatedEvent` and verify:

- `eventId` exists,
- `eventType` is `TRANSACTION_CREATED`,
- `eventTimestamp` exists,
- transaction ID,
- account ID,
- amount,
- transaction type,
- transaction-created timestamp.

The duplicate-transaction test verifies:

- repository save is not called,
- event publisher is not called.

### Controller Integration Test Isolation

Once transaction creation started publishing Kafka events, the existing controller integration test crossed the Kafka boundary unintentionally.

Replaced the real publisher bean with `@MockitoBean`.

This keeps the controller test focused on:

- HTTP,
- security,
- service/repository behavior,

while Kafka behavior is tested separately.

## Kafka Integration Test with Testcontainers

Added the Testcontainers Kafka module.

Created `TransactionEventPublisherIntegrationTest`.

### What it proves

The test executes a real round trip:

`TransactionEventPublisher -> KafkaTemplate -> JSON serialization -> real Kafka broker -> real consumer -> JSON deserialization -> assertions`

### Testcontainer

A temporary Kafka broker is started automatically for the test using `KafkaContainer`.

The test uses `kafka.getBootstrapServers()` rather than hardcoding port 9092 because Testcontainers maps ports dynamically.

### Dynamic Spring configuration

`@DynamicPropertySource` supplies:

- Testcontainer bootstrap servers,
- String key serializer,
- JSON value serializer.

### Real test consumer

The integration test creates a real Kafka consumer with:

- the Testcontainer broker,
- a dedicated test consumer group,
- `auto.offset.reset = earliest`,
- `StringDeserializer` for keys,
- `JsonDeserializer<TransactionCreatedEvent>` for values.

### Assertions

The test publishes one real `TransactionCreatedEvent`, polls Kafka, and verifies:

- a record was received,
- the Kafka record key is the transaction ID,
- the deserialized event equals the original event.

### Serialization bug found

The first real integration run failed because the effective producer value serializer was `StringSerializer`.

The mocked unit tests could not expose this because no real serialization occurred.

The integration-test Kafka properties were corrected so:

- key -> `StringSerializer`
- value -> `JsonSerializer`

The test then passed.

### Self-contained verification

The manually running Kafka Compose container was stopped.

Then:

`mvn clean test`

passed.

This proves the Kafka test suite does not depend on a manually running Kafka broker. Docker must be available for Testcontainers, but the broker lifecycle is owned by the test.

## Redis Test Infrastructure Update Before/Alongside Day 12

The Redis integration test was also made self-contained with Testcontainers before the Kafka milestone.

A manually running Redis instance is no longer required for the full Maven test suite.

## System Design Spoken Practice

Question:

How do you handle an API that writes to MySQL and then publishes an event to Kafka?

Answered correctly:

- MySQL and Kafka cannot simply be treated as one normal local transaction.
- DB success followed by Kafka failure creates inconsistency.
- Transactional outbox stores the business record and event record in the same database transaction.
- A publisher later publishes pending events and marks them published.
- Publisher crash after successful send can create duplicates.
- Consumers therefore need idempotent processing.

## SQL Practice

### Top three accounts by amount in the last 30 days

Used:

- date cutoff with `NOW() - INTERVAL 30 DAY`,
- `GROUP BY account_id`,
- `SUM(amount)`,
- descending order,
- `LIMIT 3`.

Reinforced that a direct date-range predicate is generally preferable to wrapping the timestamp column in `DATEDIFF()` for this query.

### Aggregate filtering

Correctly used `HAVING` for:

`SUM(amount) > 10000`

Reinforced:

- `WHERE` filters rows before grouping.
- `HAVING` filters groups after aggregation.

### Deterministic ordering and ranking

For equal totals, deterministic top-N ordering can use:

`ORDER BY total_amount DESC, account_id ASC`

Refreshed window functions:

- `ROW_NUMBER`: unique sequence, e.g. 1, 2, 3, 4
- `RANK`: ties share rank and gaps remain, e.g. 1, 2, 2, 4
- `DENSE_RANK`: ties share rank without gaps, e.g. 1, 2, 2, 3

## Spoken RabbitMQ vs Kafka Practice

Initial answer correctly identified:

- RabbitMQ for queue/exchange/routing-style message processing.
- Kafka for retained event streams, partition-based parallelism, consumer groups, and replay.

Refined the answer to emphasize trade-offs rather than only feature lists.

## Verification

Completed during Day 12:

- DSA tests passed.
- Kafka dependency addition did not break existing tests.
- Kafka producer unit test passed.
- Transaction service event tests passed.
- Controller integration tests passed with Kafka publisher isolated.
- Kafka Testcontainer boot test passed.
- Real Kafka publish/consume integration test passed.
- Manually running Kafka was stopped.
- Final `mvn clean test` passed with Kafka integration fully self-contained.

## Current Kafka Milestone

Completed:

- Kafka fundamentals,
- RabbitMQ comparison,
- transaction-event contract,
- partition-key decision,
- Spring Kafka producer,
- transaction-create publication,
- unit tests,
- real broker integration test,
- Testcontainers-based self-contained build,
- dual-write/outbox/idempotency system-design reasoning.

Deliberately deferred:

- application consumer,
- persistent consumer idempotency implementation,
- retry/backoff implementation,
- dead-letter topic implementation,
- transactional outbox implementation,
- schema evolution/versioning,
- production broker replication/topology,
- deeper producer reliability tuning.

## Main Learning Reinforcement Areas

- Kafka: continue reinforcing topic/partition/group/offset/lag/rebalance terminology until explanations are automatic.
- Messaging: keep RabbitMQ and Kafka mental models distinct.
- Reliability: remember that outbox solves lost-event consistency but duplicates remain possible.
- DSA: pattern recognition is improving; continue precise bookkeeping during window repair.
- SQL: maintain date predicates, aggregate filtering, deterministic ordering, and window functions through retrieval.
- System Design: continue connecting implementation decisions to failure modes and operational behavior.

## Closure Status

- Day 12 learning blocks completed.
- Final full test suite passed with manually started Kafka stopped.
- Day 12 notes and updated README generated for commit.
- Study time and final confidence scores should be captured after code/tests/docs are committed and pushed.
