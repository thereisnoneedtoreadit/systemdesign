package org.example.model;

import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class AccountTransaction {
    private final String id = UUID.randomUUID().toString();
    private final List<Account> snapshots = new ArrayList<>();
    private final List<Account> attached = new ArrayList<>();

    private final Duration lockTimeout = Duration.ofSeconds(5);

    public void snapshot(Account account) {
        snapshots.add(account.copy());
    }

    public void attach(Account account) {
        attached.add(account);
    }
}