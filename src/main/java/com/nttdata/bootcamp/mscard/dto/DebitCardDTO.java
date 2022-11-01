package com.nttdata.bootcamp.mscard.dto;

import lombok.Data;

import java.util.List;

@Data
public class DebitCardDTO {

    private Long clientId;
    private String cardNumber;
    private Integer primaryAccountId;
    private List<Long> associatedAccountsId;

}
