package org.example.model;

import lombok.Data;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class TokenBucket implements Closeable {
    private final Configuration configuration;
    private AtomicInteger balance;
    private AtomicReference<Instant> lastRefilled;

    private AtomicBoolean closed;

    public TokenBucket(Configuration configuration) {
        this.configuration = configuration;
        this.balance = new AtomicInteger(configuration.capacity);
        this.lastRefilled = new AtomicReference<>(Instant.now());
        doRefilling();
    }

    public boolean consume() {
        return balance.decrementAndGet() >= 0;
    }

    @Override
    public void close() {
        closed.set(true);
    }

    private void doRefilling() {
        while (!closed.get()) {
            if (refillRequired()) {
                this.balance.set(configuration.capacity);
                this.lastRefilled.set(Instant.now());
            }
        }
    }

    private boolean refillRequired() {
        final var refilledAt = lastRefilled.get();
        return refilledAt.plus(Duration.of(1, configuration.per.toChronoUnit())).isBefore(Instant.now())
               || refilledAt.plus(Duration.of(1, configuration.per.toChronoUnit())).equals(Instant.now());
    }

    public record Configuration(int capacity, int refill, TimeUnit per) {
    }
}
