package com.nttdata.bootcamp.mscard.service;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.model.CreditCard;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardService {

    Flux<CreditCard> findAll();

    Mono<CreditCard> create(CreditCard creditCard);

    Mono<CreditCard> findById(Integer id);

    Mono<CreditCard> update(Integer id, CreditCard creditCard);

    Mono<Void> delete(Integer id);

    Mono<String> createCreditCard(CreditCardDTO creditCardDTO);

    Flux<CreditCard> findAllByClientId(Integer id);

    Mono<String> checkFields(CreditCardDTO creditCard);
}
