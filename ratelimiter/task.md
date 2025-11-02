Token-Bucket Rate Limiter (per key)
Core: RateLimiter.allow(key) enforcing X req/sec per user.
Must handle: monotonic time, burst capacity, concurrent callers.
Stretch: sliding-window log alternative; hot-key performance; expiry of idle keys. Include clock abstraction for tests.