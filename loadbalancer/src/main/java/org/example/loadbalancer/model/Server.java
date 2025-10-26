package org.example.loadbalancer.model;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public class Server {
    public String id;
    public String host;
    public int port;
    public boolean healthy;
}