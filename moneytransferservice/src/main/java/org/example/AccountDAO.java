package org.example;

import lombok.Data;
import org.example.exception.AccountNotFoundException;
import org.example.exception.LockAcquisitionException;
import org.example.model.Account;
import org.example.model.Transaction;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AccountDAO {

    private final Map<String, AccountRow> accounts = new HashMap<>();
    private final Duration defaultLockTimeout = Duration.ofSeconds(5);

    public Optional<Account> getForUpdate(String accountId, Transaction t) {
        final var row = accounts.get(accountId);
        if (row == null) {
            return Optional.empty();
        }
        row.lock(t, defaultLockTimeout);
        return Optional.of(row.getAccount());
    }

    public void update(Account account, Transaction t) {
        final var row = accounts.get(account.id());
        if (row == null) {
            throw new AccountNotFoundException(account.id());
        }
        row.lock(t, defaultLockTimeout);
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

        public void lock(Transaction t, Duration timeout) {
            if (t.equals(heldBy)) {
                return;
            }
            if (!tryLock(timeout)) {
                throw new LockAcquisitionException();
            }
            this.heldBy = t;
        }

        public void unlock() {
            this.lock.unlock();
            this.heldBy = null;
        }

        private boolean tryLock(Duration timeout) {
            try {
                return this.lock.tryLock(timeout.getSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

}
