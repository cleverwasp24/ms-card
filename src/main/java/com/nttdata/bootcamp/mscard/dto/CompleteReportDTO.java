package com.nttdata.bootcamp.mscard.dto;

import com.nttdata.bootcamp.mscard.model.Card;
import com.nttdata.bootcamp.mscard.model.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class CompleteReportDTO {

    private Card card;
    private List<Transaction> transactions;

}
