package org.example;

import org.example.model.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AccountRepository {

    private final Map<String, Account> accounts = new HashMap<>();

    public Optional<Account> getAccount(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public void saveAccount(Account account) {
        accounts.put(account.id(), account);
    }

}
