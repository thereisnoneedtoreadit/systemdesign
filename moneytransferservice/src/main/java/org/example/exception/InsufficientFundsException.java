package org.example.exception;


public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountId) {
        super("Account with id " + accountId + " has not enough funds");
    }

}
