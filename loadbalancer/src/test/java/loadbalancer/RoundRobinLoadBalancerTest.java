package loadbalancer;


import org.example.loadbalancer.LoadBalancer;
import org.example.loadbalancer.RoundRobinLoadBalancer;
import org.example.loadbalancer.model.Server;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoundRobinLoadBalancerTest {

    private final LoadBalancer balancer = new RoundRobinLoadBalancer();

    @Test
    public void shouldRegisterAndBalanceServers() {
        Server s1 = new Server("1", "0000", 80, true);
        Server s2 = new Server("2", "0000", 80, true);
        Server s3 = new Server("3", "0000", 80, true);
        Server s4 = new Server("4", "0000", 80, true);
        Server s5 = new Server("5", "0000", 80, true);

        balancer.register(s1);
        balancer.register(s2);
        balancer.register(s3);
        balancer.register(s4);
        balancer.register(s5);

        assertEquals(s1, balancer.next());
        assertEquals(s2, balancer.next());
        assertEquals(s3, balancer.next());
        assertEquals(s4, balancer.next());
        assertEquals(s5, balancer.next());

        assertEquals(s1, balancer.next());
        assertEquals(s2, balancer.next());
        assertEquals(s3, balancer.next());
        assertEquals(s4, balancer.next());
        assertEquals(s5, balancer.next());
    }

    @Test
    public void shouldNotReturnUnhealthyServers() {
        Server s1 = new Server("1", "0000", 80, true);
        Server s2 = new Server("2", "0000", 80, false);
        Server s3 = new Server("3", "0000", 80, true);
        Server s4 = new Server("4", "0000", 80, false);
        Server s5 = new Server("5", "0000", 80, true);

        balancer.register(s1);
        balancer.register(s2);
        balancer.register(s3);
        balancer.register(s4);
        balancer.register(s5);

        assertEquals(s1, balancer.next());
        assertEquals(s3, balancer.next());
        assertEquals(s5, balancer.next());
    }

    @Test
    public void shouldReturnServersOnceHealthyAgain() {
        Server s1 = new Server("1", "0000", 80, true);
        Server s2 = new Server("2", "0000", 80, false);
        Server s3 = new Server("3", "0000", 80, true);
        Server s4 = new Server("4", "0000", 80, false);
        Server s5 = new Server("5", "0000", 80, true);

        balancer.register(s1);
        balancer.register(s2);
        balancer.register(s3);
        balancer.register(s4);
        balancer.register(s5);

        assertEquals(s1, balancer.next());
        assertEquals(s3, balancer.next());
        assertEquals(s5, balancer.next());

        s2.healthy = true;
        s4.healthy = true;

        assertEquals(s1, balancer.next());
        assertEquals(s2, balancer.next());
        assertEquals(s3, balancer.next());
        assertEquals(s4, balancer.next());
        assertEquals(s5, balancer.next());
    }

    @Test
    public void shouldNotReturnUnregisteredServers() {
        Server s1 = new Server("1", "0000", 80, true);
        Server s2 = new Server("2", "0000", 80, true);
        Server s3 = new Server("3", "0000", 80, true);
        Server s4 = new Server("4", "0000", 80, true);
        Server s5 = new Server("5", "0000", 80, true);

        balancer.register(s1);
        balancer.register(s2);
        balancer.register(s3);
        balancer.register(s4);
        balancer.register(s5);

        assertEquals(s1, balancer.next());
        assertEquals(s2, balancer.next());
        assertEquals(s3, balancer.next());
        assertEquals(s4, balancer.next());
        assertEquals(s5, balancer.next());

        balancer.deregister(s1.id);
        balancer.deregister(s3.id);

        assertEquals(s2, balancer.next());
        assertEquals(s4, balancer.next());
        assertEquals(s5, balancer.next());
    }

}
