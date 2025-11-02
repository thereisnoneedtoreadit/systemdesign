package org.example;

import java.io.Closeable;

public interface RateLimiter extends Closeable {

    boolean allow(String key);

}