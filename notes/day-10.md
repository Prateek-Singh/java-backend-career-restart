# Day 10 - Spring Security Fundamentals, Authentication Testing, DSA Revision, and Security Design

## DSA Take-Home Review

Reviewed the Day 9 take-home problem: find the length of the longest substring containing at most two distinct characters.

Pattern:

```text
sliding window + frequency map
```

Why a `HashMap` is required:

- a `Set` only tells whether a character is present
- while shrinking the window, the algorithm must know how many occurrences of each character remain
- a character should be removed from the active distinct-character state only when its frequency becomes zero

Window invariant:

```text
After shrinking completes, the active window contains at most two distinct characters,
and the frequency map exactly represents the characters in that window.
```

Implemented the solution using:

- `left` and `right` window pointers
- `HashMap<Character, Integer>` for character frequencies
- repeated shrinking with `while (map.size() > 2)`
- `maxLength` for the best valid window seen so far

Important correction during implementation: restoring the window invariant can require moving `left` more than once, so `while` is required rather than a single `if`.

Added parameterized tests covering:

- `"eceba"` -> `3`
- `"ccaabbb"` -> `5`
- `"aaaa"` -> `4`
- `"abc"` -> `2`
- empty input -> `0`
- null input -> `0`

Full Maven test suite passed after the DSA implementation.

## DSA Pattern Revision

Revised recognition signals for previously covered patterns.

### HashMap Lookup

Example: Two Sum

Recognition signal:

```text
Need to find whether a complement / previously seen value exists quickly
-> HashMap
```

Typical state:

```text
value -> index
```

### Frequency Map + Heap

Example: Top K Frequent Elements

Typical approach:

```text
frequency map
+
size-limited heap
```

A min-heap of size `k` can retain the best `k` candidates while scanning frequency entries.

### Sorting + Greedy

Example: Merge Intervals

Important correction: Merge Intervals is sorting + greedy, not sliding window.

After sorting by start time, intervals are scanned from left to right and merged whenever the next interval overlaps the current merged interval.

Invariant:

```text
All intervals before the current position have already been merged correctly.
```

### Sliding Window + Set

Example: Longest Substring Without Repeating Characters

Use a `Set` when only membership is required:

```text
Is this character already present in the active window?
```

### Sliding Window + Frequency Map

Example: Longest Substring With At Most K Distinct Characters

Use a map when additional state such as frequency must be retained:

```text
character -> active-window frequency
```

Useful distinction:

```text
membership only
-> Set

frequency / index / additional information
-> Map
```

## Backend Retrieval

Reinforced the difference between flush and commit.

```text
saveAndFlush()
-> sends pending SQL to the database
-> database constraint failures may surface immediately
-> transaction is still active

commit
-> finalizes the transaction
```

A flush is not a commit.

Reinforced concurrent duplicate behavior:

- `@Transactional` defines a unit of work
- it does not automatically serialize concurrent requests
- two transactions can both observe an existence check as false
- the database unique constraint remains the final concurrency-safe duplicate guarantee

Reinforced `@Transactional(readOnly = true)`:

- still participates in a transaction
- communicates read intent
- may enable framework/provider optimizations
- is not a security mechanism

Reinforced asynchronous acknowledgement ordering:

```text
process
-> database commit
-> acknowledge
```

Acknowledging before persistence can cause permanent message loss if the database operation later fails.

## Spring Security Fundamentals

Added Spring Security to the project along with Spring Security test support.

Observed that simply adding Spring Security changes application behavior through auto-configuration and the security filter chain. Initial controller tests failed before reaching controller logic, demonstrating that security filters execute before MVC controller processing.

## Security Contract

Defined the transaction API security direction.

All transaction endpoints require authentication:

```text
POST /transactions
GET  /transactions/{transactionId}
GET  /transactions/account/{accountId}
```

Planned authorization model:

```text
USER
-> may access only their own accounts and transactions

ADMIN
-> may access any account or transaction
```

No transaction endpoint is intentionally public.

Resource-level ownership authorization is deliberately deferred until the application has a credible persisted user/account ownership model rather than adding an artificial in-memory business mapping.

## Authentication vs Authorization

Authentication answers: who is the caller?

Authorization answers: what is the authenticated caller allowed to access?

HTTP behavior:

```text
missing or invalid authentication
-> 401 Unauthorized

authenticated but insufficient permission
-> 403 Forbidden

resource genuinely does not exist
-> 404 Not Found
```

A system may deliberately use `404` instead of `403` in some sensitive-resource designs to avoid disclosing resource existence, but the current project contract uses the explicit distinction above.

## SecurityFilterChain

Added a Spring Security configuration using `SecurityFilterChain`.

Current security decisions:

```text
/transactions/**
-> authentication required

HTTP session
-> stateless

authentication mechanism
-> HTTP Basic

CSRF
-> disabled for the current stateless Authorization-header based API
```

Configured `SessionCreationPolicy.STATELESS` so authentication is not persisted in an HTTP session. This keeps requests independent and avoids sticky-session dependence in a horizontally scaled deployment.

Important clarification: REST does not automatically imply CSRF should always be disabled. It is disabled here because authentication is supplied through the `Authorization` header rather than browser-managed session cookies.

## HTTP Basic

HTTP Basic was selected for the first security milestone rather than immediately implementing JWT.

Reason:

- isolates authentication and authorization fundamentals
- allows 401/403 behavior to be understood and tested first
- avoids introducing token signing, claims, expiry, parsing, and custom token filters before the underlying model is clear

HTTP Basic sends credentials on every request. The credentials are Base64 encoded, not encrypted, so real usage requires TLS/HTTPS.

JWT remains a later authentication enhancement after the fundamentals are stable.

## In-Memory Authentication

Added an in-memory `UserDetailsService` for the learning implementation.

Current users:

```text
user
-> ROLE_USER

admin
-> ROLE_ADMIN
```

Used BCrypt through `PasswordEncoder`.

Important terminology: BCrypt hashes passwords; it does not encrypt passwords.

The in-memory user store is a temporary learning implementation and is not positioned as the final production identity design.

## Security Matcher Debugging

A useful security bug was found while testing.

The first matcher was accidentally configured as:

```text
/transaction/**
```

while the real endpoints are:

```text
/transactions/**
```

Because the matcher did not apply, requests fell through to `.anyRequest().permitAll()` and the existing test suite passed even though the intended API protection was not working.

After correcting the matcher to `/transactions/**`, existing unauthenticated controller and integration tests correctly failed with `401 Unauthorized`.

Key lesson:

```text
green tests do not automatically mean the security requirement is correct
```

Security tests must explicitly prove that protected endpoints cannot be accessed without valid authentication.

## Controller Slice Testing and Security Configuration

`TransactionControllerTest` uses `@WebMvcTest`.

The custom `SecurityConfig` is explicitly imported into the controller slice so the tests exercise the intended application security configuration rather than unrelated/default security behavior.

Existing functional controller tests now authenticate using HTTP Basic so they can continue testing normal controller behavior such as `201`, `400`, and `404`.

## Security Tests

Added focused security tests for:

```text
missing credentials
-> 401

valid USER credentials
-> normal controller response

invalid password
-> 401
```

The unauthorized tests also verify that the service is not invoked.

This proves:

```text
request
-> SecurityFilterChain
-> authentication fails
-> controller/service are not reached
```

Controller and application integration tests were updated to supply valid HTTP Basic credentials where successful authentication is required.

Full Maven test suite passed after security integration.

## Resource-Level Authorization Design

Discussed where account ownership checks should live.

Coarse URL rules belong in `SecurityFilterChain`, for example endpoint authentication or role requirements.

Business-resource authorization belongs closer to the application/service use case, for example determining whether an authenticated user owns the requested account or transaction.

Future flow:

```text
authenticated principal
-> requested resource
-> USER: verify ownership
-> ADMIN: bypass ownership restriction
-> allow or reject
```

Full ownership authorization is intentionally deferred until a genuine user/account ownership relationship exists in the domain model.

## Security and Reliability - System Design

Reviewed authentication failure behavior.

### Invalid Credentials

Do not automatically retry the same incorrect credentials.

```text
incorrect credentials
-> 401
```

This is not a transient server failure.

### Authentication Provider Failure

Distinguished user authentication failure from authentication infrastructure failure.

```text
bad credentials
-> 401

authenticated but forbidden
-> 403

external identity provider unavailable
-> 503 Service Unavailable
```

An identity-provider outage should not cause the system to fail open and allow unauthenticated access.

### Sensitive Logging

Do not log:

- passwords
- raw `Authorization` headers
- access/refresh tokens
- secrets
- session identifiers
- unnecessary PII

### Stateless Horizontal Scaling

Avoiding server-side authentication sessions helps horizontal scaling because consecutive requests do not need to reach the same application instance.

## SQL - Conditional Aggregation

Practised per-account aggregation for:

- total transaction count
- CREDIT total
- DEBIT total
- net amount
- only accounts with at least three CREDIT transactions

Correct filtering requires conditional aggregation in the `HAVING` clause:

```sql
HAVING SUM(
    CASE
        WHEN transaction_type = 'CREDIT' THEN 1
        ELSE 0
    END
) >= 3
```

Important distinction:

```text
COUNT(*)
-> counts all transactions in the account

SUM(CASE WHEN type = 'CREDIT' THEN 1 ELSE 0 END)
-> counts only CREDIT transactions
```

## Interview Communication

Practised explaining the security implementation concisely.

Key points:

- Spring Security secures `/transactions/**`
- `SecurityFilterChain` defines HTTP security behavior
- authentication and authorization are separate concepts
- sessions are configured as stateless
- HTTP Basic is used for the first learning implementation
- BCrypt hashes passwords
- users are temporarily provided through an in-memory `UserDetailsService`
- missing/invalid credentials return 401
- 403 represents an authenticated caller without sufficient permission
- CSRF is disabled specifically because of the current stateless Authorization-header authentication model
- JWT is deliberately deferred until the fundamentals are established

## Day 10 Closure Snapshot

- Focused study time: 2 hours 30 minutes
- Confidence: Spring/Security 6/10; Testing 8/10; SQL 8/10; DSA 7/10; System Design 7/10; Docker/Compose 8/10
- Day 9 sliding-window take-home completed and tested
- DSA pattern revision completed
- Spring Security fundamentals implemented
- HTTP Basic authentication working
- stateless session handling configured
- BCrypt password hashing configured
- in-memory USER and ADMIN identities configured
- transaction endpoints protected
- controller and integration tests updated for authentication
- explicit 401 security tests added
- resource-level USER/ADMIN ownership authorization designed but intentionally deferred
- SQL conditional aggregation reinforced
- security/reliability system-design discussion completed
- full Maven test suite passed
- Git commit/push pending at the time of note generation
