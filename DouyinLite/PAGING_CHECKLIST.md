# Paging Boundary Validation Checklist

To ensure a stable and continuous feed experience, the following boundary cases have been addressed in the pagination logic:

## 1. `next_time` Handling
- [x] **Initial Load:** Request with `latest_time = null` to get the first page.
- [x] **Consecutive Loads:** Use the `next_time` returned by the previous successful request.
- [x] **Empty `next_time`:** If the API returns `null` or `0` for `next_time` and the list is not empty, it indicates no more data. Further `loadMore()` calls are blocked.
- [x] **Repeated `next_time`:** Added `lastRequestedTime` check to prevent infinite loops if the server repeatedly returns the same `next_time`.

## 2. Request Protection
- [x] **Concurrency Control:** `loadMore()` is ignored if `pagingState` is already `Loading`.
- [x] **Redundant Request Prevention:** Blocked requests if `nextTime` matches the `lastRequestedTime`.

## 3. Data Integrity & Deduplication
- [x] **ID-based Deduplication:** When appending new videos, any video whose ID already exists in the current list is filtered out.
- [x] **Error Resilience:** If a paging request fails, the `lastRequestedTime` is reset, allowing the user to retry (e.g., via manual trigger or automatic retry if implemented). Existing successfully loaded content is preserved.

## 4. Verification Methods
- [x] **Unit Tests:** `HomeViewModelTest` includes cases for successful paging, failure handling, deduplication of repeated IDs, and boundary protection for `null nextTime`.
- [x] **Logs:** (Optional) Detailed logs are placed in `HomeViewModel` for tracking state transitions (commented out in unit-test-active environments).
