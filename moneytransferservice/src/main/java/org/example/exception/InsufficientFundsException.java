package org.example.exception;


public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(java.util.UUID accountId) {
        super("Account with id " + accountId + " has not enough funds");
    }

}
