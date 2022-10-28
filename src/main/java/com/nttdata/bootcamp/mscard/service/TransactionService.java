package com.nttdata.bootcamp.mscard.service;

import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Flux<Transaction> findAll();

    Mono<Transaction> create(Transaction transaction);

    Mono<Transaction> findById(Integer id);

    Mono<Transaction> update(Integer id, Transaction transaction);

    Mono<Void> delete(Integer id);

    Mono<String> purchase(TransactionDTO transactionDTO);

    Mono<String> payDebt(TransactionDTO transactionDTO);

    Flux<Transaction> findAllByCreditCardId(Integer id);

    Mono<String> checkFields(TransactionDTO transaction);

}
