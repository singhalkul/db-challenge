package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransferControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private final String fromAccountId = "Id-from-123";
  private final String toAccountId = "Id-to-123";

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
    accountsService.createAccount(new Account(fromAccountId, new BigDecimal(1000)));
    accountsService.createAccount(new Account(toAccountId, new BigDecimal(500)));
  }

  @Test
  public void shouldTransferAmountFromOneAccountToOther() throws Exception {
    this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"fromAccountId\":\"" + fromAccountId + "\"," +
                    "\"toAccountId\":\"" + toAccountId + "\"," +
                    "\"amount\":100" +
                    "}")).andExpect(status().isOk());

    Account fromAccount = accountsService.getAccount(fromAccountId);
    assertThat(fromAccount.getBalance()).isEqualByComparingTo("900");
  }

  @Test
  public void shouldNotTransfer_WhenAmountIsNegative() throws Exception {
    this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"fromAccountId\":\"" + fromAccountId + "\"," +
                    "\"toAccountId\":\"" + toAccountId + "\"," +
                    "\"amount\":-100" +
                    "}")).andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotTransfer_WhenFromAccountDoesNotExist() throws Exception {
    this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"fromAccountId\":\"" + "Id-123" + "\"," +
                    "\"toAccountId\":\"" + toAccountId + "\"," +
                    "\"amount\":100" +
                    "}")).andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotTransfer_WhenToAccountDoesNotExist() throws Exception {
    this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"fromAccountId\":\"" + fromAccountId + "\"," +
                    "\"toAccountId\":\"" + "Id-123" + "\"," +
                    "\"amount\":100" +
                    "}")).andExpect(status().isBadRequest());
  }

  @Test
  public void shouldNotTransfer_WhenFromAccountDoesNotHaveSufficientBalance() throws Exception {
    this.mockMvc.perform(put("/v1/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"fromAccountId\":\"" + fromAccountId + "\"," +
                    "\"toAccountId\":\"" + "Id-123" + "\"," +
                    "\"amount\":2000" +
                    "}")).andExpect(status().isBadRequest());
  }
}
