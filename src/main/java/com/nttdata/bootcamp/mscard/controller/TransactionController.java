package com.nttdata.bootcamp.mscard.controller;

import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.model.Transaction;
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
@RequestMapping("/bootcamp/transaction")
public class TransactionController {

    @Autowired
    TransactionServiceImpl transactionService;

    @GetMapping(value = "/findAllTransactions")
    @ResponseBody
    public Flux<Transaction> findAllTransactions() {
        return transactionService.findAll();
    }

    @PostMapping(value = "/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> purchase(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.purchase(transactionDTO);
    }

    @PostMapping(value = "/payDebt")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> payDebt(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.payDebt(transactionDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Transaction>> findTransactionById(@PathVariable Integer id) {
        return transactionService.findById(id)
                .map(creditCard -> ResponseEntity.ok().body(creditCard))
                .onErrorResume(e -> {
                    log.info("Transaction not found " + id, e);
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Transaction>> updateTransaction(@PathVariable Integer id, @RequestBody Transaction transaction) {
        return transactionService.update(id, transaction)
                .map(a -> new ResponseEntity<>(a, HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdTransaction(@PathVariable Integer id) {
        return transactionService.delete(id)
                .defaultIfEmpty(null);
    }

    @GetMapping(value = "/findAllByCreditCardId/{id}")
    @ResponseBody
    public Flux<Transaction> findAllByCreditCardId(@PathVariable Integer id) {
        return transactionService.findAllByCreditCardId(id);
    }
}
