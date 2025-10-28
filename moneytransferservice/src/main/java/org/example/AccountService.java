package org.example;

import lombok.Data;
import org.example.exception.AccountNotFoundException;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;
import org.example.model.Transaction;

public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();

    public void transfer(String fromId, String toId, int amount) {
        final var transaction = new Transaction();
        try {
            final var from = accountDAO
                    .getForUpdate(fromId, transaction)
                    .orElseThrow(() -> new AccountNotFoundException(fromId));
            final var to = accountDAO
                    .getForUpdate(toId, transaction)
                    .orElseThrow(() -> new AccountNotFoundException(toId));

            transaction.addAccountSnapshot(from);
            transaction.addAccountSnapshot(to);

            validateBalance(from, amount);

            from.balance(from.balance() - amount);
            to.balance(to.balance() + amount);

            accountDAO.update(from, transaction);
            accountDAO.update(to, transaction);
        } catch (Exception e) {
            accountDAO.rollback(transaction);
        } finally {
            accountDAO.commit(transaction);
        }
    }

    private void validateBalance(Account account, int amount) {
        if (account.balance() < amount) {
            throw new InsufficientFundsException(account.id());
        }
    }

}
