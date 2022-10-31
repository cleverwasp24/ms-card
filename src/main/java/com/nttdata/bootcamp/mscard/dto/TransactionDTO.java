package com.nttdata.bootcamp.mscard.dto;

import lombok.Data;

@Data
public class TransactionDTO {

    private Integer cardId;
    private String description;
    private Double amount;

}
