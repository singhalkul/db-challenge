package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.CreditFailedException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferServiceTest {

  @Autowired
  private TransferService transferService;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private NotificationService notificationService;

  @After
  public void cleanup() {
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void shouldTransferAmountSuccessfully() throws CreditFailedException {
    String fromAccountId = "From-" + System.currentTimeMillis();
    String toAccountId = "To-" + System.currentTimeMillis();
    BigDecimal amount = BigDecimal.TEN;
    accountsService.createAccount(new Account(fromAccountId, new BigDecimal(1000)));
    accountsService.createAccount(new Account(toAccountId, new BigDecimal(10)));

    transferService.transfer(fromAccountId, toAccountId, amount);

    Account from = accountsService.getAccount(fromAccountId);
    Account to = accountsService.getAccount(toAccountId);
    assertThat(new BigDecimal(990)).isEqualByComparingTo("990");
    assertThat(new BigDecimal(20)).isEqualByComparingTo("20");

    verify(notificationService, Mockito.times(1)).notifyAboutTransfer(from, "Transferred amount 10 to account " + toAccountId);
    verify(notificationService, Mockito.times(1)).notifyAboutTransfer(to, "Received amount 10 from account " + fromAccountId);
  }

  @Test
  public void shouldNotTransfer_WhenFromAccountDoesNotExist() throws CreditFailedException {
    String fromAccountId = "From-" + System.currentTimeMillis();
    String toAccountId = "To-" + System.currentTimeMillis();
    BigDecimal amount = BigDecimal.TEN;

    try {
      transferService.transfer(fromAccountId, toAccountId, amount);
      fail("Should not reach here");
    } catch (AccountNotFoundException e) {
      assertThat(e.getMessage()).isEqualTo("AccountId " + fromAccountId + " does not exist or is invalid");
    }
  }

  @Test
  public void shouldNotTransfer_WhenToAccountDoesNotExist() {
    String fromAccountId = "From-" + System.currentTimeMillis();
    String toAccountId = "To-" + System.currentTimeMillis();
    BigDecimal amount = new BigDecimal(20);
    String fromBalance = "1000";
    accountsService.createAccount(new Account(fromAccountId, new BigDecimal(fromBalance)));

    try {
      transferService.transfer(fromAccountId, toAccountId, amount);
      fail("Should not reach here");
    } catch (CreditFailedException e) {
      assertThat(e.getMessage()).isEqualTo("Could not credit amount to account " + toAccountId);
    }

    Account fromAccount = accountsService.getAccount(fromAccountId);
    assertThat(fromAccount.getBalance()).isEqualByComparingTo(fromBalance);
  }

  @Test
  public void shouldNotTransfer_WhenFromAccountIsOverDraft() throws CreditFailedException {
    String fromAccountId = "From-" + System.currentTimeMillis();
    String toAccountId = "To-" + System.currentTimeMillis();
    BigDecimal amount = new BigDecimal(2000);
    accountsService.createAccount(new Account(fromAccountId, new BigDecimal(1000)));
    accountsService.createAccount(new Account(toAccountId, new BigDecimal(10)));

    try {
      transferService.transfer(fromAccountId, toAccountId, amount);
      fail("Should not reach here");
    } catch (InsufficientFundsException e) {
      assertThat(e.getMessage()).isEqualTo("Overdrafts are not supported");
    }
  }
}