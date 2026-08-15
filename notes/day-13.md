# Day 13 — Kafka Consumer, Reliability, JaCoCo, Interview/Application Readiness

## Focus

Day 13 combined technical implementation with interview/application readiness.

Technical priorities:
- Kafka consumer fundamentals and first application-consumer milestone
- consumer reliability and idempotency design
- in-session DSA
- SQL and spoken interview practice
- JaCoCo baseline

Career priorities:
- refresh the ATS CV
- start controlled applications
- update LinkedIn positioning
- introduce dedicated application/interview tracking

## 1. No-Notes Retrieval

Reviewed:
- Kafka topic vs partition
- consumer groups
- offsets
- lag
- rebalancing
- at-least-once delivery
- idempotency
- transactional outbox
- RabbitMQ exchange / queue / binding / routing key
- HTTP 401 vs 403

Main reinforcement:
- A consumer group coordinates partition ownership among consumers.
- An offset is a position within a partition; committed progress determines where a group resumes.
- At-least-once delivery can redeliver after successful business processing if offset progress was not safely recorded.
- Idempotency does not stop duplicate delivery; it prevents duplicate business effects.

## 2. DSA — Minimum Size Subarray Sum

Pattern:
- Sliding window over positive integers.

State:
- `left`
- `right`
- running `sum`
- `minLength`

Invariant:
- `sum` represents the current `[left..right]` window.

Valid candidate:
- `sum >= target`

Repair / optimization:
- Once the window reaches the target, shrink repeatedly while it is still valid.
- Update `minLength` before subtracting `nums[left]`.

Important corrections during the attempt:
- Shrinking must happen while `sum >= target`, not only once.
- Candidate length is `right - left + 1`.
- `minLength` should not start at zero; use a sentinel such as `Integer.MAX_VALUE`.
- If no valid window exists, return zero.

Final complexity:
- Time: `O(n)` because each element enters and leaves the window at most once.
- Space: `O(1)`.

Tests covered:
- normal case
- single-element answer
- no valid subarray
- full-array answer
- empty array
- null array

## 3. Kafka Consumer Fundamentals

Reviewed:
- consumer joins through a configured group ID
- partitions are assigned among consumers in the same group
- committed offsets represent consumer-group progress
- business success followed by crash before offset commit can cause redelivery
- offset commit before business success can lose the intended business effect
- duplicate delivery must be assumed under at-least-once processing

Key rule:
- Complete the business processing successfully before allowing offset progress to represent success.

## 4. Kafka Consumer Implementation

Added package:
- `com.prateek.learning.kafka.consumer`

Added:
- `TransactionCreatedEventConsumer`

Listener:
- `@KafkaListener`
- topic: `transaction-events`
- group: `transaction-created-events-cg`

The first attempt placed `@KafkaListener` at the wrong level. It was corrected to the consumer method.

## 5. Handler Boundary

Added:
- `TransactionCreatedEventHandler`
- `LoggingTransactionCreatedEventHandler`

Current flow:

```text
Kafka record
-> TransactionCreatedEventConsumer
-> TransactionCreatedEventHandler
-> LoggingTransactionCreatedEventHandler
```

Reason:
- Keep Kafka listener mechanics separate from processing/business behavior.
- Provide a clean seam for future idempotency and business-side effects.
- Make the consumer easy to unit-test without a real broker.

## 6. Kafka Consumer Unit Test

The consumer test verifies that:
- a `TransactionCreatedEvent` passed to the consumer
- is delegated exactly to `TransactionCreatedEventHandler`

This is intentionally a focused unit test rather than a broker test.

## 7. Real Kafka Consumer-Flow Integration Test

Added:
- `TransactionEventFlowIntegrationTest`
- package: `com.prateek.learning.kafka.integration`

Infrastructure:
- Testcontainers `KafkaContainer`
- image: `apache/kafka-native:4.3.1`

Dynamic properties include:
- bootstrap servers
- producer key/value serializers
- consumer key/value deserializers
- trusted JSON package
- default JSON value type
- `auto-offset-reset=earliest`

Test path:

```text
TransactionEventPublisher
-> KafkaTemplate
-> real Kafka broker
-> JSON deserialization
-> @KafkaListener
-> TransactionCreatedEventConsumer
-> mocked TransactionCreatedEventHandler
```

Because listener execution is asynchronous, verification uses a bounded Mockito timeout.

The integration test passed.

What this proves:
- application producer configuration works
- Kafka broker communication works
- JSON event deserialization works
- Spring listener registration works
- consumer group/listener flow works
- consumer delegates the received event to the processing handler

What it does not prove:
- persistent idempotency
- retries/backoff
- DLT behavior
- transactional outbox

## 8. Consumer Reliability / System Design

Designed a future persistent-idempotency model.

Suggested storage:
- `processed_events`
- `event_id` as primary key or unique constraint
- processing timestamp
- consumer/handler identifier where useful

Target processing model:

```text
receive event
-> begin local DB transaction
-> insert eventId into processed_events
-> apply business mutation
-> commit both together
-> allow Kafka offset progress
```

Critical invariant:
- The dedup record and the business side effect represent the same successful processing decision.

Failure scenario:
1. Event `E123` is processed.
2. Dedup record is persisted.
3. Business update is committed in the same local DB transaction.
4. Consumer crashes before Kafka offset progress is recorded.
5. Kafka redelivers `E123`.
6. Unique event ID reveals that it was already processed.
7. Business mutation is skipped.
8. Duplicate is treated as successfully handled.

Important correction:
- Never persist the dedup marker separately from the business mutation.
- If one succeeds and the other fails, future redelivery can either duplicate the business effect or incorrectly skip required work.

Retry reasoning:
- Retry temporary/transient failures such as DB/network timeouts or temporary downstream unavailability.
- Do not retry permanently invalid events forever.
- Poison events need bounded retry and deliberate dead-letter handling.
- Same-key ordering can influence whether later records should wait; unrelated records should not automatically be dead-lettered.

## 9. SQL Practice

Problem:
For the last 30 days, calculate each account's:
- CREDIT total
- DEBIT total
- net amount
- include accounts with aggregate transaction amount greater than 10000
- sort by net amount descending

Correct MySQL date expression:
```sql
NOW() - INTERVAL 30 DAY
```

Main corrections:
- use MySQL interval syntax
- include final `ORDER BY total_net_amount DESC`

Interview score:
- approximately 8/10; core aggregation logic was correct.

## 10. Spoken Interview Practice

Question:
“How would you design a Kafka consumer so duplicate delivery does not create duplicate business effects?”

Core answer:
- assume at-least-once delivery
- use stable `eventId`
- persist a dedup record with a unique database constraint
- commit dedup record and business mutation in the same local DB transaction
- on redelivery, detect the existing event and skip the duplicate business mutation
- only treat processing as successful after the local transaction completes

Interview score:
- approximately 8/10 after making the duplicate-detection/redelivery flow explicit.

## 11. JaCoCo Coverage

Added JaCoCo Maven reporting.

Command:
```bash
mvn clean test
```

HTML report:
```text
target/site/jacoco/index.html
```

Baseline:
- Instruction: 81%
- Branch: 77%
- Line: approximately 84.5%
- Method: approximately 84.2%
- Class: approximately 95.2%

Interpretation:
- Core transaction service/repository/mapper paths are well covered.
- Lower percentages are concentrated in older learning/demo code and simple logging/infrastructure code.
- JPA adapter branch coverage is a meaningful future review area.
- No hard coverage gate yet.
- Do not create artificial tests merely to increase a percentage.

## 12. CV and Application Readiness

Updated the ATS CV to reflect current hands-on project work accurately.

Current project positioning now includes:
- Java 21
- Spring Boot
- Spring Data JPA / MySQL
- Flyway
- Spring Security
- Redis
- Kafka producer + first consumer milestone
- Testcontainers
- JaCoCo

Truthfulness boundaries retained:
- Kafka is current hands-on project work, not professional experience.
- Do not claim JWT.
- Do not claim persistent consumer idempotency as implemented.
- Do not claim Kafka retry/DLT as implemented.
- Do not claim transactional outbox as implemented.
- Do not claim AWS deployment of the project.

Created:
- `CV_and_Applications_Tracker.md`
- `Interview_Practice_Tracker.md`

First controlled application:
- 3Pillar
- Senior Software Engineer — Java / Spring Boot / Microservices
- Remote India
- application submitted

Application details used:
- Current CTC: ₹34 LPA
- Expected CTC: ₹45 LPA
- Immediate joiner

LinkedIn:
- profile refreshed for current application phase
- Open to Work positioning updated
- primary target title: Senior Java Backend Engineer

Other job portals remain a follow-up action rather than a Day 13 technical task.

## 13. Final Verification

Final full test command:
```bash
mvn clean test
```

Result:
- PASS

Redis and Kafka integration tests remain self-contained through Testcontainers; manually started infrastructure is not required for the full Maven test run, although Docker must be available.

## Do Not Yet Claim as Implemented

- Persistent Kafka consumer idempotency/deduplication
- Kafka retry/backoff implementation
- Kafka DLT implementation
- Transactional outbox
- Kafka schema evolution/versioning
- Production Kafka topology
- JWT
- Persisted user/account ownership authorization
- AWS deployment of this project

## Next Technical Direction

Primary Kafka sequence:
1. Persistent consumer idempotency
2. Retry/backoff and DLT
3. Transactional outbox

Interview preparation should continue in parallel, with additional emphasis on:
- Core Java concurrency
- JVM/memory
- generics
- Kafka offset/commit/rebalance precision
- RabbitMQ professional recall
- broader system design

Applications should continue as a controlled stream rather than replacing technical preparation.
