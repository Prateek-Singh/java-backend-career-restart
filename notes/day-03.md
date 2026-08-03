## Java Exception Handling

- Reviewed checked and unchecked exceptions
- Reviewed `throw` versus `throws`
- Added `TransactionNotFoundException`
- Added `InvalidTransactionAmountException`
- Added `findById()` and `validateAmount()` to `TransactionService`
- Added tests using `assertThrows()` and `assertDoesNotThrow()`
- Maven build successful
- Key learning: calling `equals()` is not a JUnit assertion; assert the exception message explicitly
- Key learning: validate method arguments before checking dependent data

## Immutability and Records

- Reviewed shallow versus deep immutability
- Created immutable `TransactionSummary` class
- Used `final` fields and constructor validation
- Used `List.copyOf()` for defensive copying
- Confirmed `BigDecimal` does not require defensive copying
- Implemented value-based `equals()` and `hashCode()`
- Created equivalent `TransactionSummaryRecord`
- Added a compact record constructor for validation and defensive copying
- Added tests for validation, defensive copying, unmodifiable state, equality, and hash codes
- Key learning: record component references are final, but referenced objects may still be mutable

## DSA — Group Anagrams

- Implemented sorting-key approach using `HashMap`
- Used sorted characters as the anagram-group key
- Used `computeIfAbsent()` to create groups
- Handled null input and empty input
- Handled duplicate words and empty strings
- Added explicit validation for null words
- Avoided relying on `HashMap` iteration order in tests
- Time complexity: O(n × k log k)
- Space complexity: O(n × k)
- Maven tests passed

## SQL — Joins and Duplicate Rows

- Used `LEFT JOIN` to retain customers without accounts
- Used `INNER JOIN` and `DISTINCT` to find customers with successful transactions
- Used `COALESCE(SUM(...), 0)` for customers with no successful transactions
- Kept the transaction status filter in the `JOIN` condition to preserve outer-join behavior
- Used `GROUP BY` and `HAVING` to find customers with multiple accounts
- Reviewed one-to-many join expansion and why customer values repeat

## Spring Boot — Global Exception Handling

- Added `ApiError` record
- Added `GlobalExceptionHandler`
- Mapped `TransactionNotFoundException` to 404
- Mapped `InvalidTransactionAmountException` to 400
- Mapped `IllegalArgumentException` to 400
- Mapped unexpected exceptions to 500
- Returned a safe generic message for unexpected failures
- Added direct unit tests for HTTP status and response body
- Maven tests passed
- Key learning: the HTTP status and the status inside the error body must remain consistent