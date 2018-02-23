package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.CreditFailedException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public Account creditAccount(String accountId, BigDecimal amount) throws CreditFailedException {
    try {
      return this.accountsRepository.creditAccount(accountId, amount);
    } catch (AccountNotFoundException e) {
      throw new CreditFailedException("Could not credit amount to account " + accountId, e);
    }
  }

  public Account debitAccount(String accountId, BigDecimal amount) throws AccountNotFoundException, InsufficientFundsException {
    return this.accountsRepository.debitAccount(accountId, amount);
  }
}