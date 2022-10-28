package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.mscard.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.mscard.model.Transaction;
import com.nttdata.bootcamp.mscard.model.enums.TransactionTypeEnum;
import com.nttdata.bootcamp.mscard.service.CreditCardService;
import com.nttdata.bootcamp.mscard.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CreditCardService creditCardService;

    private TransactionDTOMapper transactionDTOMapper = new TransactionDTOMapper();

    @Override
    public Flux<Transaction> findAll() {
        log.info("Listing all transactions");
        return transactionRepository.findAll();
    }

    @Override
    public Mono<Transaction> create(Transaction transaction) {
        log.info("Creating transaction: " + transaction.toString());
        return transactionRepository.save(transaction);
    }

    @Override
    public Mono<Transaction> findById(Integer id) {
        log.info("Searching transaction by id: " + id);
        return transactionRepository.findById(id);
    }

    @Override
    public Mono<Transaction> update(Integer id, Transaction transaction) {
        log.info("Updating transaction with id: " + id + " with : " + transaction.toString());
        return transactionRepository.findById(id)
                .flatMap(t -> {
                    transaction.setId(id);
                    return transactionRepository.save(transaction);
                });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        log.info("Deleting transaction with id: " + id);
        return transactionRepository.deleteById(id);
    }

    @Override
    public Mono<String> purchase(TransactionDTO transactionDTO) {
        log.info("Credit card purchase: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.PURCHASE);
        return checkFields(transactionDTO)
                .switchIfEmpty(creditCardService.findById(transaction.getCreditCardId()).flatMap(cc -> {
                    cc.setAvailableCredit(cc.getAvailableCredit() - transaction.getAmount());
                    if (cc.getAvailableCredit() < 0) {
                        return Mono.error(new IllegalArgumentException("Insufficient credit line to purchase"));
                    }
                    transaction.setNewAvailableCredit(cc.getAvailableCredit());
                    return creditCardService.update(cc.getId(), cc)
                            .then(transactionRepository.save(transaction))
                            .then(Mono.just("Purchase done, new available credit: " + cc.getAvailableCredit()));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Credit card not found"))));
    }

    @Override
    public Mono<String> payDebt(TransactionDTO transactionDTO) {
        log.info("Credit card pay debt: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.PAY_DEBT);
        return checkFields(transactionDTO)
                .switchIfEmpty(creditCardService.findById(transaction.getCreditCardId()).flatMap(cc -> {
                    cc.setAvailableCredit(cc.getAvailableCredit() + transaction.getAmount());
                    if (cc.getAvailableCredit() > cc.getCreditLine()) {
                        return Mono.error(new IllegalArgumentException("Debt pay exceeds total credit line"));
                    }
                    transaction.setNewAvailableCredit(cc.getAvailableCredit());
                    return creditCardService.update(cc.getId(), cc)
                            .then(transactionRepository.save(transaction))
                            .then(Mono.just("Debt pay done, new available credit: " + cc.getAvailableCredit()));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Credit card not found"))));
    }

    @Override
    public Flux<Transaction> findAllByCreditCardId(Integer id) {
        log.info("Listing all transactions by credit card id");
        return transactionRepository.findAllByCreditCardId(id);
    }

    @Override
    public Mono<String> checkFields(TransactionDTO transaction) {
        if (transaction.getDescription() == null || transaction.getDescription().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Credit card transaction description cannot be empty"));
        }
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Credit card transaction amount must be greater than 0"));
        }
        return transactionRepository.findById(transaction.getId())
                .flatMap(cc -> Mono.error(new IllegalArgumentException("Credit card transaction id already exists")));
    }

}
