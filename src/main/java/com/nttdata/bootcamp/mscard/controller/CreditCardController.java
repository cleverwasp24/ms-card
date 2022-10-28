package com.nttdata.bootcamp.mscard.controller;

import com.nttdata.bootcamp.mscard.dto.CreditCardDTO;
import com.nttdata.bootcamp.mscard.model.CreditCard;
import com.nttdata.bootcamp.mscard.service.impl.CreditCardServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/bootcamp/creditcard")
public class CreditCardController {

    @Autowired
    CreditCardServiceImpl creditCardService;

    @GetMapping(value = "/findAllCreditCards")
    @ResponseBody
    public Flux<CreditCard> findAllCreditCards() {
        return creditCardService.findAll();
    }

    @PostMapping(value = "/createCreditCard")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createCreditCard(@RequestBody CreditCardDTO creditCardDTO) {
        return creditCardService.createCreditCard(creditCardDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<ResponseEntity<CreditCard>> findCreditCardById(@PathVariable Integer id) {
        return creditCardService.findById(id)
                .map(creditCard -> ResponseEntity.ok().body(creditCard))
                .onErrorResume(e -> {
                    log.info("Credit Card not found " + id, e);
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<ResponseEntity<CreditCard>> updateCreditCard(@PathVariable Integer id, @RequestBody CreditCard creditCard) {
        return creditCardService.update(id, creditCard)
                .map(a -> new ResponseEntity<>(a, HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdCreditCard(@PathVariable Integer id) {
        return creditCardService.delete(id)
                .defaultIfEmpty(null);
    }

    @GetMapping(value = "/findAllByClientId/{id}")
    @ResponseBody
    public Flux<CreditCard> findAllByClientId(@PathVariable Integer id) {
        return creditCardService.findAllByClientId(id);
    }


}
