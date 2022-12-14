package com.nttdata.bootcamp.mscard.controller;

import com.nttdata.bootcamp.mscard.dto.*;
import com.nttdata.bootcamp.mscard.model.Card;
import com.nttdata.bootcamp.mscard.model.Transaction;
import com.nttdata.bootcamp.mscard.service.TransactionService;
import com.nttdata.bootcamp.mscard.service.impl.CardServiceImpl;
import com.nttdata.bootcamp.mscard.service.impl.TransactionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/bootcamp/card")
public class CardController {

    @Autowired
    CardServiceImpl cardService;

    @Autowired
    TransactionService transactionService;

    @GetMapping(value = "/findAllCards")
    @ResponseBody
    public Flux<Card> findAllCards() {
        return cardService.findAll();
    }

    @PostMapping(value = "/createCreditCard")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createCreditCard(@RequestBody CreditCardDTO creditCardDTO) {
        return cardService.createCreditCard(creditCardDTO);
    }

    @PostMapping(value = "/createDebitCard")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createDebitCard(@RequestBody DebitCardDTO debitCardDTO) {
        return cardService.createDebitCard(debitCardDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Card>> findtCardById(@PathVariable Long id) {
        return cardService.findById(id)
                .map(creditCard -> ResponseEntity.ok().body(creditCard))
                .onErrorResume(e -> {
                    log.info("Card not found " + id, e);
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Card>> updateCard(@PathVariable Long id, @RequestBody Card card) {
        return cardService.update(id, card)
                .map(a -> new ResponseEntity<>(a, HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdCard(@PathVariable Long id) {
        return cardService.delete(id)
                .defaultIfEmpty(null);
    }

    @GetMapping(value = "/findAllByClientId/{id}")
    @ResponseBody
    public Flux<Card> findAllByClientId(@PathVariable Long id) {
        return cardService.findAllByClientId(id);
    }

    @GetMapping(value = "/findAllCreditByClientId/{id}")
    @ResponseBody
    public Flux<Card> findAllCreditByClientId(@PathVariable Long id) {
        return cardService.findAllCreditByClientId(id);
    }

    @GetMapping(value = "/findAllDebitByClientId/{id}")
    @ResponseBody
    public Flux<Card> findAllDebitByClientId(@PathVariable Long id) {
        return cardService.findAllDebitByClientId(id);
    }


    @GetMapping(value = "/getDailyBalanceReportCurrentMonth/{id}")
    @ResponseBody
    public Mono<CardReportDTO> getDailyBalanceReportCurrentMonth(@PathVariable Long id) {
        return cardService.findById(id)
                .flatMap(card -> transactionService.generateCardReportCurrentMonth(card.getId()))
                .switchIfEmpty(Mono.error(new Exception("Card not found")));
    }

    @GetMapping(value = "/getDailyBalanceReport/{id}")
    @ResponseBody
    public Mono<CardReportDTO> getDailyBalanceReport(@PathVariable Long id, @RequestBody PeriodDTO periodDTO) {
        return cardService.findById(id)
                .flatMap(card -> transactionService.generateCardReport(card.getId(), periodDTO))
                .switchIfEmpty(Mono.error(new Exception("Card not found")));
    }

    @GetMapping(value = "/getLatestTenTransactions/{id}")
    @ResponseBody
    public Flux<Transaction> getLatestTenTransactions(@PathVariable Long id) {
        return cardService.findById(id)
                .flatMapMany(card -> transactionService.findAllByCardIdDesc(card.getId()).take(10))
                .switchIfEmpty(Mono.error(new Exception("Card not found")));
    }

    @GetMapping(value = "/getCompleteReport/{id}")
    @ResponseBody
    public Mono<CompleteReportDTO> getCompleteReport(@PathVariable Long id, @RequestBody PeriodDTO periodDTO) {
        return cardService.findById(id)
                .flatMap(card -> transactionService.generateCompleteReport(card.getId(), periodDTO))
                .switchIfEmpty(Mono.error(new Exception("Card not found")));
    }

}
