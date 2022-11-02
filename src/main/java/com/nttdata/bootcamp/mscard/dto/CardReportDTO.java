package com.nttdata.bootcamp.mscard.dto;

import com.nttdata.bootcamp.mscard.model.Card;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CardReportDTO {

    private Card card;
    private List<DailyBalanceDTO> dailyBalances = new ArrayList<>();

}
