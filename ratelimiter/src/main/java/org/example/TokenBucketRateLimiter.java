package org.example;

import lombok.RequiredArgsConstructor;
import org.example.model.TokenBucket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiter {

    private final Rate rate;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean allow(String key) {
        final var bucket = buckets.computeIfAbsent(key, k -> initBucket());
        return bucket.consume();
    }

    private TokenBucket initBucket() {
        return new TokenBucket(new TokenBucket.Configuration(rate.requests, rate.requests, rate.per));
    }

    @Override
    public void close() {
        buckets.values().forEach(TokenBucket::close);
    }

    public record Rate(int requests, TimeUnit per) {}

}
