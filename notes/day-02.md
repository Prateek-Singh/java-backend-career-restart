# Day 2

## Java Collections and Equality

- Reviewed `==` versus `equals()`
- Implemented `equals()` and `hashCode()`
- Verified duplicate handling in `HashSet`
- Explored the risks of mutating fields used in `equals()` and `hashCode()`
- Fixed an incorrect setter implementation
- Added and ran JUnit tests
- Key learning: identity fields should ideally be immutable when objects are stored in hash-based collections

## Valid Anagram

- Completed a sorting-based solution
- Completed a frequency-array solution
- Added 14 JUnit tests
- Sorting complexity: O(n log n) time, O(n) space
- Optimized complexity: O(n) time, O(1) space
- Key learning: a fixed 26-element frequency array uses constant space

## SQL Refresh

- Practiced `SELECT`, `WHERE`, `ORDER BY`, `GROUP BY`, `SUM`, `COUNT`, `AVG`, and `HAVING`
- Completed 5 aggregation queries
- Key learning: `WHERE` filters rows before grouping, while `HAVING` filters grouped results

## Interview Communication Practice

- Practiced explaining a backend data-processing flow
- Focused on persistence, asynchronous messaging, retries, idempotency, and scale
- Improved explanation length and structure across multiple attempts
- Key learning: speak from anchor points instead of memorizing exact sentences

## Summary

- Total focused time: 2 hours 50 minutes
- Overall confidence: 7/10
- Main challenge: understanding `equals()`, `hashCode()`, and `HashSet.contains()`
- Strongest learning: sorting and frequency-array approaches for Valid Anagram