package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConcurrentTransfersTest {

  @Autowired
  private TransferService transferService;

  @Autowired
  private AccountsService accountsService;

  @Before
  public void cleanup() {
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void performConcurrentTransfers() throws InterruptedException {
    int eachAccountBalance = 100_000;
    int numberOfAccounts = 10;
    List<Account> accounts = createAccounts(eachAccountBalance, numberOfAccounts);
    ExecutorService service = Executors.newFixedThreadPool(20);
    List<Callable<Void>> tasks = buildTransferTasks(accounts, 10_00_000);

    List<Future<Void>> result = service.invokeAll(tasks);
    waitForTasksToFinish(result);

    List<Account> updatedAccounts = accounts.stream().map(account -> accountsService.getAccount(account.getAccountId())).collect(Collectors.toList());
    BigDecimal bankTotal = updatedAccounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

    Integer expectedBalance = eachAccountBalance * numberOfAccounts;
    assertThat(bankTotal).isEqualByComparingTo(new BigDecimal(expectedBalance));

  }

  private void waitForTasksToFinish(List<Future<Void>> result) {
    result.forEach(r -> {
      try {
        r.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    });
  }

  private List<Account> createAccounts(int eachAccountBalance, int numberOfAccounts) {
    List<Account> accounts = IntStream.rangeClosed(1, numberOfAccounts)
            .mapToObj(i -> new Account("ConcurrentTest-Acc-" + i, BigDecimal.valueOf(eachAccountBalance)))
            .collect(Collectors.toList());

    accounts.forEach(acc -> accountsService.createAccount(acc));
    return accounts;
  }

  private List<Callable<Void>> buildTransferTasks(List<Account> accounts, int numberOfTransfers) {
    int maxAmountToTransfer = 1000;

    return IntStream.rangeClosed(1, numberOfTransfers).mapToObj(i -> (Callable<Void>) () -> {
      int fromIndex = (int) ((accounts.size() - 1) * Math.random());
      Account from = accounts.get(fromIndex);

      int toIndex;
      do {
        toIndex = (int) ((accounts.size() - 1) * Math.random());
      } while (toIndex == fromIndex);
      Account to = accounts.get(toIndex);

      int amount = (int) (maxAmountToTransfer * Math.random());

      transferService.transfer(from.getAccountId(), to.getAccountId(), new BigDecimal(amount));

      return null;
    }).collect(Collectors.toList());
  }
}