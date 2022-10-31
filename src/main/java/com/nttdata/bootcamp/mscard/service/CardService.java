package com.nttdata.bootcamp.mscard.service;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.dto.DebitCardDTO;
import com.nttdata.bootcamp.mscard.model.Card;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CardService {

    Flux<Card> findAll();

    Mono<Card> create(Card card);

    Mono<Card> findById(Long id);

    Mono<Card> update(Long id, Card card);

    Mono<Void> delete(Long id);

    Mono<String> createCreditCard(CreditCardDTO creditCardDTO);

    Mono<String> createDebitCard(DebitCardDTO debitCardDTO);

    Flux<Card> findAllByClientId(Long id);

    Flux<Card> findAllCreditByClientId(Long id);

    Flux<Card> findAllDebitByClientId(Long id);

    Mono<String> checkFieldsCC(CreditCardDTO creditCard);

    Mono<String> checkFieldsDC(DebitCardDTO debitCardDTO);
}
