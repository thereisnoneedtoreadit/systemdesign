1. In-memory Load Balancer
   Core: implement LoadBalancer with register(Server s), deregister(String id), next() using round-robin.
   Must handle: empty pool (exception), duplicate ids, concurrent register/next.
   Stretch: plug-in strategies (ROUND_ROBIN, LEAST_CONNECTIONS), health checks (mark down), weighting. Write minimal unit tests.