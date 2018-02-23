package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
              "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    Account account = accounts.get(accountId);
    if (account == null)
      throw new AccountNotFoundException("AccountId " + accountId + " does not exist or is invalid");
    return account;
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

  @Override
  public Account debitAccount(String accountId, BigDecimal amount) throws InsufficientFundsException, AccountNotFoundException {
    return performThreadSafeUpdate(accountId, (id, acc) -> debit(acc, amount));
  }

  @Override
  public Account creditAccount(String accountId, BigDecimal amount) throws AccountNotFoundException {
    return performThreadSafeUpdate(accountId, (id, acc) -> credit(amount, acc));
  }

  private Account performThreadSafeUpdate(String accountId, BiFunction<String, Account, Account> updateFunction) throws AccountNotFoundException {
    Account account = accounts.computeIfPresent(accountId, updateFunction);
    if (account == null)
      throw new AccountNotFoundException("AccountId " + accountId + " does not exist or is invalid");
    return account;
  }

  private Account debit(Account acc, BigDecimal amount) {
    BigDecimal balance = acc.getBalance().subtract(amount);
    if (isBalanceGreaterThanZero(balance)) {
      return new Account(acc.getAccountId(), balance);
    }
    throw new InsufficientFundsException("Overdrafts are not supported");
  }

  private boolean isBalanceGreaterThanZero(BigDecimal balance) {
    return balance.compareTo(BigDecimal.ZERO) > 0;
  }

  private Account credit(BigDecimal amount, Account acc) {
    return new Account(acc.getAccountId(), acc.getBalance().add(amount));
  }
}