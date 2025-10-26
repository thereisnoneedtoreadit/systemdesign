package org.example.loadbalancer.model;

public record Server(
        String id, String host, int port, boolean healthy
) {
}
