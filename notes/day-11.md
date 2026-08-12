# Day 11 - Redis Caching, Resilience, DSA Precision, Security Retrieval, and SQL

## Day 11 Goals

- Complete and review Longest Repeating Character Replacement.
- Improve DSA precision around state, invariant, invalid condition, repair rule, and answer update.
- Reinforce Spring Security fundamentals through no-notes retrieval.
- Design Redis caching before implementation.
- Add Redis-backed Spring Cache for transaction lookup by ID.
- Define and verify graceful degradation when Redis is unavailable.
- Add focused cache tests and a real Redis integration test.
- Keep SQL and system-design reasoning active.

## DSA - Longest Repeating Character Replacement

Problem:

Given an uppercase string `s` and integer `k`, return the length of the longest substring that can be transformed into one repeated character by replacing at most `k` characters.

Examples:

```text
"ABAB", k=2 -> 4
"AABABBA", k=1 -> 4
```

### Pattern

Sliding window + frequency map.

### State

- `left` - start of the active window.
- `right` - character currently being added.
- frequency map - count of each character in the window.
- `maxFrequency` - highest useful character frequency seen while expanding.
- `maxLength` - best window length found.

### Invalid-window condition

```text
windowLength - maxFrequency > k
```

The difference represents the number of replacements required to make the window consist of one repeated character.

### Important optimization

The common O(n) solution keeps `maxFrequency` monotonic and does not recompute it downward when `left` moves.

A stale `maxFrequency` may mean the physical working window is not always strictly valid according to the exact current counts, but it does not produce an incorrect larger answer. The algorithm is maximizing achievable window length rather than maintaining an exact-current-window validity proof after every shrink.

### Bugs corrected during implementation

- `k == 0` must not return 0. Zero replacements still allows existing repeated-character substrings.
- Corrected a map lookup that accidentally treated a character value as a String index.
- Reinforced updating the answer after window repair.
- Reinforced the use of a repeated repair condition where required.

### Test coverage

Parameterized tests include:

```text
"ABAB", 2 -> 4
"AABABBA", 1 -> 4
"AAAA", 0 -> 4
"AABBB", 0 -> 3
"ABCD", 1 -> 2
"", 2 -> 0
null, 2 -> 0
```

### DSA process improvement

The main current DSA gap is no longer only pattern recognition. The larger improvement area is translating a recognized pattern into precise state transitions and code.

Before coding future DSA problems, explicitly state:

```text
Pattern
Why this pattern
State
Invariant
Invalid condition
Repair rule
When the answer is safe to update
Edge cases
```

From Day 11 onward, DSA will be completed inside the study session rather than assigned as take-home work unless explicitly requested.

## Spring Security Retrieval

Reviewed without notes:

- Authentication: establishes who the caller is.
- Authorization: determines what an authenticated caller may access.
- `SecurityFilterChain` runs before controller execution.
- HTTP Basic extracts credentials from the Authorization header.
- Spring Security authentication machinery uses configured user details and password verification.
- `SecurityContext` stores the successful `Authentication` result for the current request/thread.
- `UserDetailsService` loads configured user details.
- BCrypt hashes passwords; hashing is one-way while encryption is reversible with the required key.
- Missing/invalid credentials -> `401 Unauthorized`.
- Authenticated but insufficient permission -> `403 Forbidden`.
- `SessionCreationPolicy.STATELESS` means requests do not depend on server-side login session state.
- Stateless authentication supports horizontal scaling because requests can land on different application instances.
- CSRF is disabled for the current stateless Authorization-header authentication model, not merely because the application is RESTful.
- HTTP Basic remains deliberate for learning the security fundamentals before adding JWT/token lifecycle complexity.

## Redis Cache Design

### Chosen cache candidate

```http
GET /transactions/{transactionId}
```

This is a better first cache candidate than account transaction lists because transactions are effectively immutable after creation in the current API.

Caching:

```http
GET /transactions/account/{accountId}
```

would require more complex invalidation because new transactions continually change the account-level result.

### Cache contract

```text
Source of truth       MySQL
Strategy              Cache-aside through Spring Cache
Cache hit             Return cached transaction, skip DB
Cache miss            Query DB -> cache result -> return
Missing transaction   Return 404; do not negative-cache initially
TTL                   30 minutes
Key namespace         transactions::<transactionId>
Value format          JSON
Redis unavailable     Fall back to MySQL
Future update/delete  Invalidate cache after successful DB change
```

### Why no negative caching initially

A transaction may be missing now and created later. Caching a 404 could return a stale not-found result until the negative-cache TTL expires unless creation also invalidates the negative entry.

### TTL reasoning

A 30-minute TTL is reasonable because transactions are currently immutable after creation. Stale-data risk is very low, while a longer TTL improves cache effectiveness and reduces repeated DB reads.

If update/delete support is introduced later, explicit invalidation would be required rather than relying only on TTL.

## Redis Infrastructure and Spring Cache

Added:

- `spring-boot-starter-cache`
- `spring-boot-starter-data-redis`
- Redis 7.4 in Docker Compose
- Compose service-name networking using `redis`
- Redis host/port application configuration
- cache enablement for the JPA/Redis runtime path
- `@Cacheable` on transaction lookup by ID
- 30-minute Redis cache TTL
- JSON value serialization
- namespaced cache keys
- short Redis connect/command timeouts

Verified Redis container health with:

```bash
docker exec -it transaction-redis redis-cli ping
```

Result:

```text
PONG
```

Manual POST/GET requests verified that the first transaction lookup populates Redis and subsequent lookup can be served from cache.

## Redis Serialization Debugging

The first cache-enabled integration request returned HTTP 500.

The actual root cause was initially hidden by the generic API error response. `GlobalExceptionHandler` was improved to log unexpected exceptions while continuing to return a safe generic error body to clients.

The logged root cause was:

```text
org.springframework.data.redis.serializer.SerializationException: Cannot serialize
JdkSerializationRedisSerializer
```

The default cache serializer attempted JDK serialization.

Rather than making the domain model implement `Serializable`, Redis cache values were configured to use JSON serialization. This keeps cached values inspectable and avoids tighter coupling to Java-native serialization.

The Redis JSON mapper is configured with Java-time module support so `Instant` can be serialized/deserialized correctly.

## Exception Logging

Unexpected exceptions are now logged with their stack traces while clients continue to receive:

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

The client contract avoids leaking infrastructure details.

Logging must still avoid secrets, passwords, raw Authorization headers, tokens, or unnecessary sensitive data.

## Redis Failure Resilience

The first Redis-down test returned HTTP 500 because the default cache error handling propagated `RedisConnectionFailureException`.

A custom `RedisCacheErrorHandler` was added.

Desired and verified flow:

```text
Redis GET fails
-> custom cache error handler logs warning
-> service continues as cache miss
-> MySQL lookup succeeds
-> Redis PUT fails
-> cache error handler logs warning
-> API returns DB result
```

Manual verification while Redis was unavailable showed:

```text
Cache GET failed...
Hibernate: select ...
Cache PUT failed...
HTTP 200
```

This proves Redis is a performance optimization rather than a hard dependency for transaction reads.

### Failure trade-off

Redis failure preserves correctness and API availability only while MySQL can absorb the redirected traffic.

Potential impact during Redis outage:

- increased API latency
- higher DB CPU / I/O
- more DB connections
- reduced cache-hit ratio
- possible DB saturation
- observability alerts

## Cache Stampede and Hot Keys

### Cache stampede

Many concurrent requests miss the same cache entry before it is populated, causing many requests to hit the DB at once.

### Hot key

A single Redis key receives disproportionately high traffic and can overload the Redis node/shard serving that key even when the value is cached.

A hot key expiration can also trigger a cache stampede.

Mitigations were discussed but deliberately not implemented in the first Redis milestone.

## Cache Testing

### Existing service tests

`TransactionServiceTest` remains a pure Mockito unit test. It does not verify `@Cacheable` because the service is instantiated directly and therefore bypasses Spring's caching proxy.

### Spring cache-proxy test

Added `TransactionServiceCacheTest` using:

- a small Spring test context
- `@EnableCaching`
- mocked `TransactionRepository`
- `ConcurrentMapCacheManager`

The test calls `getTransactionById()` twice and verifies:

```text
two service calls
-> one repository call
```

This proves the second call is intercepted by Spring's cache proxy.

### Cache error-handler tests

Added `RedisCacheErrorHandlerTest` covering:

- cache GET failure
- cache PUT failure
- cache EVICT failure
- cache CLEAR failure

Each verifies that the handler does not rethrow the cache exception.

### Real Redis integration test

Added a focused Redis integration test using:

- real Redis on `localhost:6379`
- real Redis cache manager
- real JSON cache serialization
- mocked transaction repository
- Spring caching proxy

The narrow test context explicitly provides the dependencies normally supplied by Spring Boot auto-configuration:

- `ObjectMapper`
- `RedisConnectionFactory`
- `CacheManager`
- `RedisCacheErrorHandler`

The test verifies:

```text
first lookup
-> repository called
-> value written to Redis

second lookup
-> value read/deserialized from Redis
-> repository not called again
```

This exercise also reinforced what Spring Boot auto-configuration normally creates on behalf of the application.

## System Design / Redis Retrieval

Reinforced:

- Cache is not automatically a source of truth.
- Graceful degradation can preserve correctness while still causing a performance incident.
- A cache failure handler without short timeouts can still produce unacceptable latency.
- Cache hit ratio is an important production metric.
- DB capacity must account for cache-outage fallback traffic.
- Invalidation complexity should influence what data is cached.
- Do not add a cache simply because Redis is available; justify the data and failure contract first.

## SQL Reinforcement

Wrote per-account conditional aggregation for:

- total transaction count
- CREDIT total
- DEBIT total
- net amount
- accounts with at least three CREDIT transactions

Correct pattern:

```sql
select account_id,
       count(*) as total_transactions,
       sum(case when transaction_type = 'CREDIT' then amount else 0 end) as total_credit,
       sum(case when transaction_type = 'DEBIT' then amount else 0 end) as total_debit,
       sum(case when transaction_type = 'CREDIT' then amount else 0 end) -
       sum(case when transaction_type = 'DEBIT' then amount else 0 end) as net_amount
from transactions
group by account_id
having count(case when transaction_type = 'CREDIT' then 1 end) >= 3;
```

Also reinforced the equivalent explicit conditional-count form:

```sql
having sum(case when transaction_type = 'CREDIT' then 1 else 0 end) >= 3;
```

## Verification

- Redis container verified with `PING -> PONG`.
- Real cache values verified manually in Redis.
- Redis-down fallback manually verified with HTTP `200`.
- Spring cache-proxy test passed.
- Redis cache error-handler tests passed.
- Real Redis integration test passed.
- Final `mvn clean test` passed with Redis running for the real Redis integration test.

## Day 11 Process Change

From Day 11 onward:

- DSA will be completed as part of the study session.
- No routine DSA take-home will be assigned.
- Each problem will include pattern recognition, explicit state/invariant reasoning, manual walkthrough, implementation, review, complexity, edge cases, and tests within the same session.
- Take-home DSA will only be used if explicitly requested.

## Day 11 Closure Status

- DSA: completed in-session.
- Spring Security retrieval: completed.
- Redis design and implementation: completed for the current cache foundation.
- Redis happy path and failure path: verified.
- Cache tests: added and passing.
- SQL reinforcement: completed.
- Final `mvn clean test`: passed.
- Git commit/push: pending at the time these notes were generated.
