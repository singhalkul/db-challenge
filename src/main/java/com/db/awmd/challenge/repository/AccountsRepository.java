package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;

import java.math.BigDecimal;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId) throws AccountNotFoundException;

  void clearAccounts();

  Account debitAccount(String accountId, BigDecimal amount) throws InsufficientFundsException, AccountNotFoundException;

  Account creditAccount(String accountId, BigDecimal amount) throws AccountNotFoundException;
}
