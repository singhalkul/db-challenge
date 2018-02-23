package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.CreditFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class TransferService {

  private AccountsService accountsService;

  private NotificationService notificationService;

  @Autowired
  public TransferService(AccountsService accountsService, NotificationService notificationService) {
    this.accountsService = accountsService;
    this.notificationService = notificationService;
  }

  public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) throws CreditFailedException {
    Account updatedFrom = accountsService.debitAccount(fromAccountId, amount);
    Account updatedTo = tryTransferringToAccount(fromAccountId, toAccountId, amount);

    notificationService.notifyAboutTransfer(updatedFrom, "Transferred amount " + amount + " to account " + toAccountId);
    notificationService.notifyAboutTransfer(updatedTo, "Received amount " + amount + " from account " + fromAccountId);
  }

  private Account tryTransferringToAccount(String fromAccountId, String toAccountId, BigDecimal amount) throws CreditFailedException {
    try {
      return accountsService.creditAccount(toAccountId, amount);
    } catch (CreditFailedException e) {
      log.error("An error occurred trying to transfer money to account " + toAccountId + ". " +
              "Reverting money deducted from account" + fromAccountId, e);
      refundMoneyIntoFromAccount(fromAccountId, amount);
      throw e;
    }
  }

  private void refundMoneyIntoFromAccount(String fromAccountId, BigDecimal amount) throws CreditFailedException {
    accountsService.creditAccount(fromAccountId, amount);
    log.info("Successfully reverted money to account " + fromAccountId);
  }
}