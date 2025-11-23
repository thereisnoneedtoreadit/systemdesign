package org.example.model;

import lombok.Getter;
import lombok.SneakyThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class TokenBucket {
    private final Configuration configuration;
    private final AtomicInteger balance;
    private final AtomicReference<Instant> lastRefilled;
    private final AtomicBoolean refilling;

    public TokenBucket(Configuration configuration) {
        this.configuration = configuration;
        this.balance = new AtomicInteger(configuration.capacity);
        this.lastRefilled = new AtomicReference<>(Instant.now());
        this.refilling = new AtomicBoolean(false);
    }

    public boolean consume() {
        refill();
        return balance.getAndUpdate(b -> Math.max(b - 1, 0)) > 0;
    }

    private void refill() {
        if (refillRequired()) {
            boolean claimed = refilling.compareAndSet(false, true);
            if (!claimed) {
                return;
            }
            this.balance.getAndUpdate(b -> Math.min(b + configuration.refill, configuration.capacity));
            this.lastRefilled.set(Instant.now());
            refilling.set(false);
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
