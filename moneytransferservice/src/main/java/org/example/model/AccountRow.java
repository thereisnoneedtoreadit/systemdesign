package org.example.model;

import lombok.Getter;
import lombok.SneakyThrows;
import org.example.exception.LockAcquisitionException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class AccountRow {
    private final ReentrantLock lock;
    private AccountTransaction lockedBy;
    private Account account;

    public AccountRow(Account account) {
        this.account = account;
        this.lockedBy = null;
        this.lock = new ReentrantLock();
    }

    public void update(Account account, AccountTransaction t) {
        if (holding(t)) {
            this.account = account.copy();
        } else {
            withLock(() -> this.account = account, t);
        }
    }

    public void lock(AccountTransaction t, Duration timeout) {
        if (holding(t)) {
            return;
        }
        if (!tryLock(timeout)) {
            throw new LockAcquisitionException();
        }
        this.lockedBy = t;
        imitateAwaiting();
    }

    public void release() {
        this.lock.unlock();
        this.lockedBy = null;
    }

    private boolean holding(AccountTransaction t) {
        return t.equals(lockedBy);
    }

    private void withLock(Runnable runnable, AccountTransaction t) {
        try {
            tryLock(t.getLockTimeout());
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    private boolean tryLock(Duration timeout) {
        try {
            return this.lock.tryLock(timeout.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.lock.unlock();
            return false;
        }
    }


    @SneakyThrows
    private void imitateAwaiting() {
        Thread.sleep(1000);
    }
}