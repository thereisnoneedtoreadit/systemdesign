import lombok.SneakyThrows;
import org.example.AccountDAO;
import org.example.AccountService;
import org.example.exception.InsufficientFundsException;
import org.example.model.Account;
import org.example.model.AccountTransaction;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceIntegrationTest {

    private final AccountDAO dao = new AccountDAO();
    private final AccountService accountService = new AccountService(dao);

    @Test
    public void shouldTransferMoney() {
        final var account1 = createAccount(100);
        final var account2 = createAccount(200);

        accountService.transfer(account1.id(), account2.id(), 50);

        assertEquals(50, dao.get(account1.id()).balance());
        assertEquals(250, dao.get(account2.id()).balance());
    }

    @Test
    @SneakyThrows
    public void shouldTransferMoney_twoSimultaneousTransfers() {
        final var account1 = createAccount(100);
        final var account2 = createAccount(150);

        // account1 sends 50, receives 100 (+50)
        Future<?> op1 = runAsync(() -> accountService.transfer(account1.id(), account2.id(), 50));
        // account2 sends 100, receives 50 (-50)
        Future<?> op2 = runAsync(() -> accountService.transfer(account2.id(), account1.id(), 100));

        op1.get();
        op2.get();

        assertEquals(150, dao.get(account1.id()).balance());
        assertEquals(100, dao.get(account2.id()).balance());
    }

    @Test
    @SneakyThrows
    public void shouldTransferMoney_balanceValidated() {
        final var account1 = createAccount(100);
        final var account2 = createAccount(150);

        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.transfer(account1.id(), account2.id(), 101));

        assertEquals(100, dao.get(account1.id()).balance());
        assertEquals(150, dao.get(account2.id()).balance());
    }

    private Account createAccount(int balance) {
        final var transaction = new AccountTransaction();
        final var account = Account
                .builder()
                .id(UUID.randomUUID())
                .balance(balance)
                .build();
        dao.save(account, transaction);
        dao.commit(transaction);
        return account;
    }

    private Future<?> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable);
    }

}
