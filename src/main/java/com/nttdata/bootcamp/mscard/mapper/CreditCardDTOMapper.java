package com.nttdata.bootcamp.mscard.mapper;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.model.CreditCard;
import com.nttdata.bootcamp.mscard.model.enums.CreditCardTypeEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class CreditCardDTOMapper {

    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public CreditCardDTO convertToDto(CreditCard creditCard){
        return modelMapper.map(creditCard, CreditCardDTO.class);
    }
    public CreditCard convertToEntity(CreditCardDTO creditCardDTO, CreditCardTypeEnum type) {
        CreditCard creditCard = modelMapper.map(creditCardDTO, CreditCard.class);
        creditCard.setCreditCardType(type.ordinal());
        creditCard.setAvailableCredit(creditCardDTO.getCreditLine());
        creditCard.setCreationDate(LocalDateTime.now());
        return creditCard;
    }

}
