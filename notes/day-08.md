# Day 8 - Dockerized Application, Concurrency Design, Merge Intervals, and Conditional Aggregation

## Main backend milestone

- Started with no-notes retrieval of the persistence flow:
  `request -> controller -> service -> TransactionRepository -> JpaTransactionRepositoryAdapter -> TransactionEntityMapper -> SpringDataTransactionRepository -> MySQL`.
- Reinforced that Flyway owns versioned schema migrations and `flyway_schema_history`, while Hibernate with `ddl-auto=validate` only checks entity/schema compatibility.
- Reinforced the separation between the generated persistence UUID primary key and the business transaction ID used as the idempotency key.
- Reinforced that an application-level duplicate check cannot guarantee uniqueness under concurrency; the database unique constraint is the final guard.
- Reinforced the use of `Instant` for an unambiguous global transaction creation moment rather than `LocalDateTime`.

## Docker and Docker Compose

- Added a multi-stage `Dockerfile`.
- Build stage uses Maven with Java 21 to compile and package the application.
- Runtime stage uses a Java 21 JRE image and copies only the packaged Spring Boot JAR.
- Built the image successfully with:

```bash
docker build -t transaction-service .
```

- Ran the application container independently and connected it to the existing host-accessible MySQL using `host.docker.internal` during the intermediate verification step.
- Manually verified POST and GET transaction REST calls against the containerized application.
- Extended the root-level `compose.yml` so both the Spring Boot application and MySQL run in the same Compose stack.
- Changed the JDBC host inside Compose from `localhost` / `host.docker.internal` to the Compose service name:

```text
jdbc:mysql://mysql:3306/transaction_db
```

- Verified that Docker's internal service discovery resolves `mysql` from the application container.
- Verified POST and GET calls through the final Compose stack.
- Verified transaction data remains available after:

```bash
docker compose stop
docker compose start
```

because MySQL uses a named volume.
- Reinforced the distinction between `--build` (rebuild image before startup) and `-d` (detached mode); they can be combined with `docker compose up --build -d`.
- Noted a hardening follow-up: `depends_on` controls startup order but does not guarantee MySQL readiness. Add a health check when needed.

## System design - concurrent duplicate transactions

Scenario: a REST request and an asynchronous message carry the same business transaction ID at nearly the same time.

- Both callers can pass an application `exists` check before either insert commits.
- `@Transactional` does not by itself eliminate this race under normal concurrent execution.
- The database unique constraint on the business transaction ID remains the final concurrency-safe guard.
- One insert succeeds; the losing insert receives a unique-constraint violation.
- REST design discussion: map a confirmed duplicate to `409 Conflict`.
- Async/Kafka design discussion: when the unique violation is specifically the expected duplicate key, treat the event as already processed and acknowledge it rather than retrying forever.
- Do not classify every database exception as a duplicate; transient database failures require retry/backoff and eventual DLQ/recovery handling.
- Service-level transaction boundaries are preferred for the complete create-transaction unit of work rather than placing the transaction only around the repository `save()` call.

Important: the explicit `409` mapping and service `@Transactional` boundary were discussed as design decisions on Day 8 and should not be described as implemented until code and tests exist.

## DSA - Merge Intervals

Implemented Merge Intervals using sorting plus a greedy scan.

Core rule after sorting intervals by start:

```text
if next.start <= current.end
    merge
else
    finalize current
```

Merged end:

```text
max(current.end, next.end)
```

Complexity:

- Time: `O(n log n)` due to sorting; merge scan is `O(n)`.
- Result space: `O(n)` in the worst case.
- Added a defensive deep copy of the `int[][]` input so sorting and interval-end updates do not mutate caller-owned inner arrays.

Testing:

- Added JUnit 5 parameterized tests with `@MethodSource`.
- Covered overlapping intervals, touching intervals, a single interval, contained intervals, unsorted input, null input, and empty input.
- Full Maven test suite passed.

## SQL - conditional aggregation

Learned the `CASE` expression for conditional aggregation.

Example pattern:

```sql
SELECT
    account_id,
    SUM(CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE 0 END) AS total_credit_amount,
    SUM(CASE WHEN transaction_type = 'DEBIT' THEN amount ELSE 0 END) AS total_debit_amount,
    COUNT(*) AS transaction_count
FROM transactions
GROUP BY account_id
HAVING COUNT(*) >= 3;
```

Key learning:

- `SUM(CASE ... THEN amount ELSE 0 END)` totals matching amounts.
- `SUM(CASE ... THEN 1 ELSE 0 END)` counts matching rows.
- `COUNT(CASE WHEN ... THEN 1 END)` also counts matching rows because non-matching rows produce `NULL`.
- `COUNT(CASE WHEN ... THEN 1 ELSE 0 END)` is incorrect for conditional counting because both `1` and `0` are non-null.
- Use `GROUP BY` when the required output is one row per group; a window function is unnecessary when individual rows do not need to be preserved.

## Interview communication

Practised:

> Why use `TransactionRepository` plus `JpaTransactionRepositoryAdapter` instead of injecting `JpaRepository` directly into `TransactionService`?

Key points:

- Keeps the service coupled to an application-facing abstraction rather than Spring Data JPA.
- Separates domain/service concerns from persistence-specific APIs and entity mapping.
- Allows service tests to use an in-memory implementation or mocks.
- Allows the JPA adapter to be integration-tested independently.
- Trade-off: additional classes and mapping code; may be unnecessary for a very small CRUD application.

## Documentation review

- Found that the README error-response example documented `timestamp` and `path`, while the actual `ApiError` record currently contains only `status`, `error`, and `message`.
- README should document the current API response accurately and should not present planned behavior as implemented.
- README Docker documentation should now describe the full application + MySQL Compose stack and use the actual filename `compose.yml`.
- Spring Security, Redis, Kafka, AWS, and AI remain planned work until implemented and tested.

## Planned roadmap additions confirmed on Day 8

### Spring Security

Planned only, after a concrete API authentication/authorization use case is defined. Cover `SecurityFilterChain`, authentication versus authorization, `401` versus `403`, stateless REST security, roles/authorities, authenticated tests, password encoding, and JWT only after the basic model is understood.

### AWS

Hands-on refresh tied to the project rather than broad certification study. Planned core services: IAM, Secrets Manager, RDS/Aurora MySQL, S3, CloudWatch, VPC/security-group basics, ALB, and one practical container deployment path such as ECS/Fargate. Additional services such as Lambda, SQS, SNS, API Gateway, ElastiCache, or MSK should only be added where the project has a credible use case.

### AI learning

Add a backend-engineer-focused AI track after the core backend foundation is stronger. Cover LLM fundamentals, calling an LLM from Java/Spring, structured outputs/tool calling, embeddings/vector search and RAG only where justified, plus timeout/retry behavior, rate limits, security, prompt injection, PII/privacy, latency, cost, evaluation, and observability. Do not shift the roadmap toward model training or data-science-heavy work unless the target roles change.

## Day 8 status

- Main backend milestone: completed.
- Docker image build: passed.
- Application + MySQL Compose stack: verified.
- POST/GET through containerized app: passed.
- Persistence after Compose restart: verified.
- Merge Intervals tests: passed.
- Full Maven test suite: passed.
- Git commit/push: pending at the time of this note.
