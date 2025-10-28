package org.example.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class Transaction {
    private String id = UUID.randomUUID().toString();
    private List<Account> accountSnapshots = new ArrayList<>();

    public void addAccountSnapshot(Account account) {
        accountSnapshots.add(account);
    }
}