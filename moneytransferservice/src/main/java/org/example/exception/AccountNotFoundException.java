package org.example.exception;


public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountId) {
        super("Account with id " + accountId + " not found");
    }

}
