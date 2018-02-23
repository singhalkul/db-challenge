package com.db.awmd.challenge.domain;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferDto {

  @NotNull
  @NotEmpty
  private final String fromAccountId;

  @NotNull
  @NotEmpty
  private final String toAccountId;

  @NotNull
  @Min(value = 0, message = "Transfer amount must be positive")
  private final BigDecimal amount;

}
