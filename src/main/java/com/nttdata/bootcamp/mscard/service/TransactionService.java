package com.nttdata.bootcamp.mscard.service;

import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Flux<Transaction> findAll();

    Mono<Transaction> create(Transaction transaction);

    Mono<Transaction> findById(Long id);

    Mono<Transaction> update(Long id, Transaction transaction);

    Mono<Void> delete(Long id);

    Mono<String> creditPurchase(TransactionDTO transactionDTO);

    Mono<String> payDebt(TransactionDTO transactionDTO);

    Mono<String> debitPurchase(TransactionDTO transactionDTO);

    Mono<String> debitDeposit(TransactionDTO transactionDTO);

    Flux<Transaction> findAllByCardId(Long id);

    Mono<String> checkFields(TransactionDTO transaction);

}
