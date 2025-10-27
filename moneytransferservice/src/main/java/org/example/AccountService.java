package org.example;

import org.example.exception.AccountNotFoundException;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;

public class AccountService {

    private final AccountRepository accountRepository = new AccountRepository();

    public void transfer(String fromId, String toId, int amount) {
        final var from = accountRepository
                .getAccount(fromId)
                .orElseThrow(() -> new AccountNotFoundException(fromId));
        final var to = accountRepository
                .getAccount(toId)
                .orElseThrow(() -> new AccountNotFoundException(toId));

        validateBalance(from, amount);

        from.balance(from.balance() - amount);
        to.balance(to.balance() + amount);
    }

    private void validateBalance(Account account, int amount) {
        if (account.balance() < amount) {
            throw new InsufficientFundsException(account.id());
        }
    }

}
