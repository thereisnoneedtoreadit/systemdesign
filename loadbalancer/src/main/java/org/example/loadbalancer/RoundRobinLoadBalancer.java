package org.example.loadbalancer;

import org.example.loadbalancer.model.Server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private final Map<String, Server> servers = new LinkedHashMap<>();
    private int nextServer = 0;

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void register(Server s) {
        withLock(() -> servers.put(s.id(), s));
    }

    @Override
    public void deregister(String id) {
        withLock(() -> servers.remove(id));
    }

    @Override
    public Server next() {
        return withLock(() -> {
                    if (servers.isEmpty()) {
                        return null;
                    }
                    if (servers.size() <= nextServer) {
                        nextServer = 0;
                    }
                    Server server = getServerByIndex(nextServer);
                    calculateNext();
                    return server;
                }
        );
    }

    private Server getServerByIndex(int index) {
        return new ArrayList<>(servers.values()).get(index);
    }

    private void calculateNext() {
        if (servers.size() <= nextServer + 1) {
            nextServer = 0;
        } else {
            nextServer++;
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
