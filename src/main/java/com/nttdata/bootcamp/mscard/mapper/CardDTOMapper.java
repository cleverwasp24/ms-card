package com.nttdata.bootcamp.mscard.mapper;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.dto.DebitCardDTO;
import com.nttdata.bootcamp.mscard.model.Card;
import com.nttdata.bootcamp.mscard.model.enums.CardTypeEnum;
import com.nttdata.bootcamp.mscard.model.enums.ClientCardTypeEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class CardDTOMapper {

    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public Object convertToDto(Card card) {
        return switch (CardTypeEnum.valueOf(card.getCardType())) {
            case CREDIT -> modelMapper.map(card, CreditCardDTO.class);
            case DEBIT -> modelMapper.map(card, DebitCardDTO.class);
        };
    }

    public Card convertToEntity(Object creditCardDTO, CardTypeEnum cType, ClientCardTypeEnum ccType) {
        Card card = modelMapper.map(creditCardDTO, Card.class);
        card.setCardType(cType.ordinal());
        card.setClientCardType(ccType.ordinal());
        switch (cType) {
            case CREDIT:
                card.setAvailableCredit(card.getCreditLine());
                break;
        }
        card.setCreationDate(LocalDateTime.now());
        return card;
    }

}
