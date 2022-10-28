package com.nttdata.bootcamp.mscard.dto;

import lombok.Data;

@Data
public class CreditCardDTO {

    private Integer id;
    private Integer clientId;
    private String creditCardNumber;
    private Double creditLine;

}
