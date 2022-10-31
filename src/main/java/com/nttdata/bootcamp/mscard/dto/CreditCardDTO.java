package com.nttdata.bootcamp.mscard.dto;

import lombok.Data;

@Data
public class CreditCardDTO {

    private Long clientId;
    private String cardNumber;
    private Double creditLine;

}
