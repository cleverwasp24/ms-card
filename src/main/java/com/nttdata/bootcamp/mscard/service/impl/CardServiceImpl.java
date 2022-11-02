package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.dto.DebitCardDTO;
import com.nttdata.bootcamp.mscard.infrastructure.CardRepository;
import com.nttdata.bootcamp.mscard.mapper.CardDTOMapper;
import com.nttdata.bootcamp.mscard.model.Card;
import com.nttdata.bootcamp.mscard.model.enums.CardTypeEnum;
import com.nttdata.bootcamp.mscard.model.enums.ClientCardTypeEnum;
import com.nttdata.bootcamp.mscard.service.CardService;
import com.nttdata.bootcamp.mscard.service.DatabaseSequenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ClientServiceImpl clientService;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private DatabaseSequenceService databaseSequenceService;

    private CardDTOMapper cardDTOMapper = new CardDTOMapper();

    @Override
    public Flux<Card> findAll() {
        log.info("Listing all credit cards");
        return cardRepository.findAll();
    }

    @Override
    public Mono<Card> create(Card card) {
        log.info("Creating credit card: " + card.toString());
        return cardRepository.save(card);
    }

    @Override
    public Mono<Card> findById(Long id) {
        log.info("Searching credit card by id: " + id);
        return cardRepository.findById(id);
    }

    @Override
    public Mono<Card> update(Long id, Card card) {
        log.info("Updating credit card with id: " + id + " with : " + card.toString());
        return cardRepository.findById(id).flatMap(c -> {
            card.setId(id);
            return cardRepository.save(card);
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting credit card with id: " + id);
        return cardRepository.deleteById(id);
    }

    @Override
    public Mono<String> createCreditCard(CreditCardDTO creditCardDTO) {
        log.info("Creating credit card: " + creditCardDTO.toString());
        return checkFieldsCC(creditCardDTO).switchIfEmpty(clientService.findById(creditCardDTO.getClientId()).flatMap(c -> {
            Card card = cardDTOMapper.convertToEntity(creditCardDTO, CardTypeEnum.CREDIT, ClientCardTypeEnum.valueOf(c.getClientType()));
            return databaseSequenceService.generateSequence(Card.SEQUENCE_NAME).flatMap(s -> {
                card.setId(s);
                return cardRepository.save(card)
                        .flatMap(cc -> Mono.just("Credit card created! " + cardDTOMapper.convertToDto(cc)));
            });
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Mono<String> createDebitCard(DebitCardDTO debitCardDTO) {
        log.info("Creating debit card: " + debitCardDTO.toString());
        return checkFieldsDC(debitCardDTO).switchIfEmpty(clientService.findById(debitCardDTO.getClientId()).flatMap(c -> {
            return accountService.findAllByClientId(c.getId()).collectList().flatMap(l -> {
                if (l.isEmpty()) {
                    return Mono.error(new IllegalArgumentException("Associated accounts not found"));
                } else {
                    List<Long> ids = l.stream().map(a -> a.getId()).collect(Collectors.toList());
                    if (ids.containsAll(debitCardDTO.getAssociatedAccountsId())) {
                        Card card = cardDTOMapper.convertToEntity(debitCardDTO, CardTypeEnum.DEBIT, ClientCardTypeEnum.valueOf(c.getClientType()));
                        card.setCreditLine(l.get(0).getBalance());
                        return databaseSequenceService.generateSequence(Card.SEQUENCE_NAME).flatMap(s -> {
                            card.setId(s);
                            return cardRepository.save(card)
                                    .flatMap(dc -> Mono.just("Debit card created! " + cardDTOMapper.convertToDto(dc)));
                        });
                    } else {
                        return Mono.error(new IllegalArgumentException("Associated accounts not valid"));
                    }
                }
            });
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Flux<Card> findAllByClientId(Long id) {
        log.info("Listing all cards by client id");
        return cardRepository.findAllByClientId(id);
    }

    @Override
    public Flux<Card> findAllCreditByClientId(Long id) {
        log.info("Listing all credit cards by client id");
        return cardRepository.findAllByClientIdAndCardType(id, CardTypeEnum.CREDIT.ordinal());
    }

    @Override
    public Flux<Card> findAllDebitByClientId(Long id) {
        log.info("Listing all debit cards by client id");
        return cardRepository.findAllByClientIdAndCardType(id, CardTypeEnum.DEBIT.ordinal());
    }

    @Override
    public Mono<String> checkFieldsCC(CreditCardDTO card) {
        if (card.getCardNumber() == null || card.getCardNumber().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Credit card number cannot be empty"));
        }
        if (card.getCreditLine() == null || card.getCreditLine() <= 0) {
            return Mono.error(new IllegalArgumentException("Credit card credit line must be greater than 0"));
        }
        return Mono.empty();
    }

    @Override
    public Mono<String> checkFieldsDC(DebitCardDTO card) {
        if (card.getCardNumber() == null || card.getCardNumber().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Debit card number cannot be empty"));
        }
        if (card.getPrimaryAccountId() == null || card.getPrimaryAccountId() <= 0) {
            return Mono.error(new IllegalArgumentException("Debit card primary account is not valid"));
        }
        if (card.getAssociatedAccountsId() == null || card.getAssociatedAccountsId().size() < 1) {
            return Mono.error(new IllegalArgumentException("Debit card must have at least 1 associated account"));
        }
        return Mono.empty();
    }

}
