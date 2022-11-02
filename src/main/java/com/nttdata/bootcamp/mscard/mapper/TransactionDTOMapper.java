package com.nttdata.bootcamp.mscard.mapper;

import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.model.Transaction;
import com.nttdata.bootcamp.mscard.model.enums.TransactionTypeEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class TransactionDTOMapper {

    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public TransactionDTO convertToDto(Transaction transaction) {
        return modelMapper.map(transaction, TransactionDTO.class);
    }

    public Transaction convertToEntity(TransactionDTO transactionDTO, TransactionTypeEnum type) {
        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
        transaction.setTransactionType(type.ordinal());
        switch (type) {
            case CREDIT_PURCHASE:
                transaction.setDescription("Credit card purchase -$" + transactionDTO.getAmount());
                break;
            case PAY_DEBT:
                transaction.setDescription("Credit card pay debt +$" + transactionDTO.getAmount());
                break;
            case DEBIT_PURCHASE:
                transaction.setDescription("Debit card purchase -$" + transactionDTO.getAmount());
                break;
            case DEPOSIT:
                transaction.setDescription("Debit card deposit +$" + transactionDTO.getAmount());
                break;
        }
        transaction.setTransactionDate(LocalDateTime.now());
        return transaction;
    }
}