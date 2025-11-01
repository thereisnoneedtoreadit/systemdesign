package org.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;
import org.example.model.AccountTransaction;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountDAO dao;

    public void transfer(UUID fromId, UUID toId, int amount) {
        final var transaction = new AccountTransaction();
        try {
            final var accounts = getAccountsForUpdate(fromId, toId, transaction);

            transaction.snapshot(accounts.from);
            transaction.snapshot(accounts.to);

            validateBalance(accounts.from, amount);

            accounts.from.balance(accounts.from.balance() - amount);
            accounts.to.balance(accounts.to.balance() + amount);

            dao.save(accounts.from, transaction);
            dao.save(accounts.to, transaction);
        } catch (Exception e) {
            dao.rollback(transaction);
            throw e;
        } finally {
            dao.commit(transaction);
        }
    }

    private TransferAccounts getAccountsForUpdate(UUID fromId, UUID toId, AccountTransaction transaction) {
        if (fromId.compareTo(toId) < 0) {
            final var from = dao.getForUpdate(fromId, transaction);
            final var to = dao.getForUpdate(toId, transaction);
            return new TransferAccounts(from, to);
        } else {
            final var to = dao.getForUpdate(toId, transaction);
            final var from = dao.getForUpdate(fromId, transaction);
            return new TransferAccounts(from, to);
        }
    }

    private void validateBalance(Account account, int amount) {
        if (account.balance() < amount) {
            throw new InsufficientFundsException(account.id());
        }
    }

    private record TransferAccounts(Account from, Account to){}

}
