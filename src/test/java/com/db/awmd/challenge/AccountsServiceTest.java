package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.CreditFailedException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @After
  public void cleanup() {
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123", new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void shouldThrowExceptionForGetAccount_whenAccountDoesNotExist() {
    String accountId = "Id-123";

    try {
      accountsService.getAccount(accountId);
      fail("should not reach here");
    } catch (AccountNotFoundException e) {
      assertThat(e.getMessage()).isEqualTo("AccountId " + accountId + " does not exist or is invalid");
    }
  }

  @Test
  public void shouldAddAmountToAnExistingAccount() throws CreditFailedException {
    String accountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(accountId, new BigDecimal(1000));
    accountsService.createAccount(account);

    Account updated = accountsService.creditAccount(accountId, new BigDecimal(100));

    assertThat(updated.getBalance()).isEqualByComparingTo("1100");
  }

  @Test
  public void shouldSubtractAmountFromAnExistingAccount() {
    String accountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(accountId, new BigDecimal(1000));
    accountsService.createAccount(account);

    Account updated = accountsService.debitAccount(accountId, new BigDecimal(100));

    assertThat(updated.getBalance()).isEqualByComparingTo("900");
  }

  @Test
  public void shouldThrowException_WhenAccountHasInsufficientFunds() {
    String accountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(accountId, new BigDecimal(1000));
    accountsService.createAccount(account);

    try {
      accountsService.debitAccount(accountId, new BigDecimal(1100));
      fail("should not reach here");
    } catch (InsufficientFundsException e) {
      assertThat(e.getMessage()).isEqualTo("Overdrafts are not supported");
    }

    Account currentStatus = accountsService.getAccount(accountId);
    assertThat(currentStatus.getBalance()).isEqualByComparingTo("1000");
  }
}