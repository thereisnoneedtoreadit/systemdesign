package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.AccountNotFoundException;
import org.example.model.Account;
import org.example.model.AccountRow;
import org.example.model.AccountTransaction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Slf4j
public class AccountDAO {

    private final Map<UUID, AccountRow> accounts = new ConcurrentHashMap<>();

    public Account get(UUID accountId) {
        final var row = accounts.get(accountId);
        if (row == null) {
            throw new AccountNotFoundException(accountId);
        }
        return row.getAccount().copy();
    }

    public Account getForUpdate(UUID accountId, AccountTransaction t) {
        final var row = accounts.get(accountId);
        if (row == null) {
            throw new AccountNotFoundException(accountId);
        }
        row.lock(t, t.getLockTimeout());
        log.info("locked for update account {}. transaction {}", accountId, t.getId());
        return row.getAccount().copy();
    }

    public void save(Account account, AccountTransaction t) {
        t.attach(account);
        log.info("saved account {}. transaction {}", account.id(), t.getId());
    }

    public void rollback(AccountTransaction t) {
        persistAndRelease(t.getSnapshots(), t);
        log.info("rolled back transaction {}", t.getId());
    }

    public void commit(AccountTransaction t) {
        persistAndRelease(t.getAttached(), t);
        log.info("committed transaction {}", t.getId());
    }

    private void persistAndRelease(List<Account> accs, AccountTransaction t) {
        accs
                .forEach(account -> accounts.compute(account.id(), (id, row) -> {
                    if (row == null) {
                        return new AccountRow(account.copy());
                    } else {
                        row.update(account, t);
                        row.release();
                        return row;
                    }
                }));
    }

}
