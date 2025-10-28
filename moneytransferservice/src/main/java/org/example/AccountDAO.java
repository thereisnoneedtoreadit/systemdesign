package org.example;

import lombok.Data;
import org.example.exception.AccountNotFoundException;
import org.example.model.Account;
import org.example.model.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class AccountDAO {

    private final Map<String, AccountRow> accounts = new HashMap<>();

    public Optional<Account> getForUpdate(String accountId, Transaction t) {
        final var row = accounts.get(accountId);
        if (row == null) {
            return Optional.empty();
        }
        row.lock(t);
        return Optional.of(row.getAccount());
    }

    public void update(Account account, Transaction t) {
        final var row = accounts.get(account.id());
        if (row == null) {
            throw new AccountNotFoundException(account.id());
        }
        row.lock(t);
        accounts.put(account.id(), row);
    }

    public void rollback(Transaction t) {
        t.accountSnapshots().forEach(snapshot -> update(snapshot, t));
    }

    public void commit(Transaction t) {
        accounts.values().forEach(row -> {
            if (t.equals(row.heldBy)) {
                row.unlock();
            }
        });
    }

    @Data
    private static class AccountRow {
        private Account account;
        private Transaction heldBy;
        private ReentrantLock lock = new ReentrantLock();

        public void lock(Transaction t) {
            if (t.equals(heldBy)) {
                return;
            }
            this.lock.lock();
            this.heldBy = t;
        }

        public void unlock() {
            this.lock.unlock();
            this.heldBy = null;
        }
    }

}
