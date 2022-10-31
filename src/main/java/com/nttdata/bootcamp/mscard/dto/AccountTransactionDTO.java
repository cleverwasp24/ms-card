package com.nttdata.bootcamp.mscard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountTransactionDTO {

    private Long accountId;
    private Double amount;


}
