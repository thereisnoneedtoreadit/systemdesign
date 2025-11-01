package org.example.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Account {
    private final UUID id;
    private int balance;

    public Account copy() {
        return this.toBuilder().build();
    }
}
