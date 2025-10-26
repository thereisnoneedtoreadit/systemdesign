package org.example.loadbalancer;

import org.example.loadbalancer.model.Server;

public interface LoadBalancer {

    void register(Server s);

    void deregister(String id);

    Server next();

}
