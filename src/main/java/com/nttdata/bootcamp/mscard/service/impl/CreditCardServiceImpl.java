package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.infrastructure.CreditCardRepository;
import com.nttdata.bootcamp.mscard.mapper.CreditCardDTOMapper;
import com.nttdata.bootcamp.mscard.model.CreditCard;
import com.nttdata.bootcamp.mscard.model.enums.CreditCardTypeEnum;
import com.nttdata.bootcamp.mscard.service.CreditCardService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class CreditCardServiceImpl implements CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private ClientServiceImpl clientService;

    private CreditCardDTOMapper creditCardDTOMapper = new CreditCardDTOMapper();

    @Override
    public Flux<CreditCard> findAll() {
        log.info("Listing all credit cards");
        return creditCardRepository.findAll();
    }

    @Override
    public Mono<CreditCard> create(CreditCard creditCard) {
        log.info("Creating credit card: " + creditCard.toString());
        return creditCardRepository.save(creditCard);
    }

    @Override
    public Mono<CreditCard> findById(Integer id) {
        log.info("Searching credit card by id: " + id);
        return creditCardRepository.findById(id);
    }

    @Override
    public Mono<CreditCard> update(Integer id, CreditCard creditCard) {
        log.info("Updating credit card with id: " + id + " with : " + creditCard.toString());
        return creditCardRepository.findById(id)
                .flatMap(c -> {
                    creditCard.setId(id);
                    return creditCardRepository.save(creditCard);
                });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        log.info("Deleting credit card with id: " + id);
        return creditCardRepository.deleteById(id);
    }

    @Override
    public Mono<String> createCreditCard(CreditCardDTO creditCardDTO) {
        log.info("Creating credit card: " + creditCardDTO.toString());

        return checkFields(creditCardDTO)
                .switchIfEmpty(clientService.findById(creditCardDTO.getClientId())
                        .flatMap(c -> {
                            CreditCard creditCard = creditCardDTOMapper.convertToEntity(creditCardDTO, CreditCardTypeEnum.valueOf(c.getClientType()));
                            return creditCardRepository.save(creditCard)
                                    .then(Mono.just("Credit card created! "
                                            + creditCardDTOMapper.convertToDto(creditCard)));
                        })
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Flux<CreditCard> findAllByClientId(Integer id) {
        log.info("Listing all credit cards by client id");
        return creditCardRepository.findAllByClientId(id);
    }

    @Override
    public Mono<String> checkFields(CreditCardDTO creditCard) {
        if (creditCard.getCreditCardNumber() == null || creditCard.getCreditCardNumber().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Credit card number cannot be empty"));
        }
        if (creditCard.getCreditLine() == null || creditCard.getCreditLine() <= 0) {
            return Mono.error(new IllegalArgumentException("Credit card credit line must be greater than 0"));
        }
        return creditCardRepository.findById(creditCard.getId())
                .flatMap(cc -> Mono.error(new IllegalArgumentException("Credit card id already exists")));
    }

}
