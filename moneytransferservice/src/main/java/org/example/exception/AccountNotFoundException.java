package org.example.exception;


public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(java.util.UUID accountId) {
        super("Account with id " + accountId + " not found");
    }

}
