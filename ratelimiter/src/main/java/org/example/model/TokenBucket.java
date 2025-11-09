package org.example.model;

import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class TokenBucket implements Closeable {
    private final Configuration configuration;
    private final ExecutorService refiller;
    private final AtomicInteger balance;
    private final AtomicReference<Instant> lastRefilled;
    private final AtomicBoolean closed;

    public TokenBucket(Configuration configuration) {
        this.configuration = configuration;
        this.refiller = Executors.newSingleThreadExecutor();
        this.balance = new AtomicInteger(configuration.capacity);
        this.lastRefilled = new AtomicReference<>(Instant.now());
        this.closed = new AtomicBoolean(false);
        refiller.submit(this::doRefilling);
    }

    public boolean consume() {
        return balance.getAndUpdate(b -> Math.max(b - 1, 0)) >= 0;
    }

    @Override
    public void close() {
        closed.set(true);
    }

    @SneakyThrows
    private void doRefilling() {
        while (!closed.get()) {
            if (refillRequired()) {
                this.balance.getAndUpdate(b -> Math.min(b + configuration.refill, configuration.capacity));
                this.lastRefilled.set(Instant.now());
            }
        }
        Thread.sleep(1000);
    }

    private boolean refillRequired() {
        final var refilledAt = lastRefilled.get();
        return refilledAt.plus(Duration.of(1, configuration.per.toChronoUnit())).isBefore(Instant.now())
               || refilledAt.plus(Duration.of(1, configuration.per.toChronoUnit())).equals(Instant.now());
    }

    public record Configuration(int capacity, int refill, TimeUnit per) {
    }
}
