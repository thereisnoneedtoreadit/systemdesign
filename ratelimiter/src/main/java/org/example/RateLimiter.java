package org.example;

public interface RateLimiter {

    boolean allow(String key);

}