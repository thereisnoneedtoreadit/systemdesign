package org.example.exception;


public class LockAcquisitionException extends RuntimeException {

    public LockAcquisitionException() {
        super("Unable to acquire lock");
    }

}
