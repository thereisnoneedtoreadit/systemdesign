package org.example.loadbalancer;

import org.example.loadbalancer.model.Server;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private final Map<String, Server> registry = new HashMap<>();
    private final Deque<String> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void register(Server server) {
        validate(server);
        withLock(() -> {
            Server updated = registry.put(server.id, server);
            if (updated == null) {
                queue.addLast(server.id);
            }
        });
    }

    @Override
    public void deregister(String id) {
        withLock(() -> {
            registry.remove(id);
        });
    }

    @Override
    public Server next() {
        return withLock(() -> {
                    if (registry.isEmpty()) {
                        return null;
                    }
                    return getAndEnqueueHealthyServer();
                }
        );
    }

    private Server getAndEnqueueHealthyServer() {
        int attempts = queue.size();
        for(int i = 0; i < attempts; i++) {
            Server candidate = registry.get(queue.pollFirst());
            if (candidate == null) {
                continue;
            }
            if (!candidate.healthy) {
                queue.addLast(candidate.id);
                continue;
            }
            queue.addLast(candidate.id);
            return candidate;
        }
        return null;
    }

    private void validate(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("server is null");
        }
        if (server.id == null) {
            throw new IllegalArgumentException("server.id is null");
        }
    }

    private void withLock(Runnable function) {
        try {
            lock.lock();
            function.run();
        } finally {
            lock.unlock();
        }
    }

    private <T> T withLock(Supplier<T> function) {
        try {
            lock.lock();
            return function.get();
        } finally {
            lock.unlock();
        }
    }

}
