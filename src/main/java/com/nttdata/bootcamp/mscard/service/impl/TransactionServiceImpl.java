package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.AccountTransactionDTO;
import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.mscard.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.mscard.model.Transaction;
import com.nttdata.bootcamp.mscard.model.enums.TransactionTypeEnum;
import com.nttdata.bootcamp.mscard.service.CardService;
import com.nttdata.bootcamp.mscard.service.DatabaseSequenceService;
import com.nttdata.bootcamp.mscard.service.TransactionService;
import lombok.Data;
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
    private CardService cardService;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private DatabaseSequenceService databaseSequenceService;

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
    public Mono<Transaction> findById(Long id) {
        log.info("Searching transaction by id: " + id);
        return transactionRepository.findById(id);
    }

    @Override
    public Mono<Transaction> update(Long id, Transaction transaction) {
        log.info("Updating transaction with id: " + id + " with : " + transaction.toString());
        return transactionRepository.findById(id)
                .flatMap(t -> {
                    transaction.setId(id);
                    return transactionRepository.save(transaction);
                });
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting transaction with id: " + id);
        return transactionRepository.deleteById(id);
    }

    @Override
    public Mono<String> creditPurchase(TransactionDTO transactionDTO) {
        log.info("Credit card purchase: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.CREDIT_PURCHASE);
        return checkFields(transactionDTO)
                .switchIfEmpty(cardService.findById(transaction.getCardId()).flatMap(cc -> {
                    cc.setAvailableCredit(cc.getAvailableCredit() - transaction.getAmount());
                    if (cc.getAvailableCredit() < 0) {
                        return Mono.error(new IllegalArgumentException("Insufficient credit line to purchase"));
                    }
                    transaction.setNewAvailableCredit(cc.getAvailableCredit());
                    return cardService.update(cc.getId(), cc)
                            .flatMap(c -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(seq -> {
                                transaction.setId(seq);
                                return transactionRepository.save(transaction)
                                        .flatMap(t -> Mono.just("Purchase done, new available credit: " + cc.getAvailableCredit()));
                            }));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Credit card not found"))));
    }

    @Override
    public Mono<String> payDebt(TransactionDTO transactionDTO) {
        log.info("Credit card pay debt: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.PAY_DEBT);
        return checkFields(transactionDTO)
                .switchIfEmpty(cardService.findById(transaction.getCardId()).flatMap(cc -> {
                    cc.setAvailableCredit(cc.getAvailableCredit() + transaction.getAmount());
                    if (cc.getAvailableCredit() > cc.getCreditLine()) {
                        return Mono.error(new IllegalArgumentException("Debt pay exceeds total credit line"));
                    }
                    transaction.setNewAvailableCredit(cc.getAvailableCredit());
                    return cardService.update(cc.getId(), cc)
                            .flatMap(c -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(seq -> {
                                transaction.setId(seq);
                                return transactionRepository.save(transaction)
                                        .flatMap(t -> Mono.just("Debt pay done, new available credit: " + cc.getAvailableCredit()));
                            }));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Credit card not found"))));
    }

    @Override
    public Mono<String> debitPurchase(TransactionDTO transactionDTO) {
        log.info("Debit card purchase: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.DEBIT_PURCHASE);
        return checkFields(transactionDTO)
                .switchIfEmpty(cardService.findById(transaction.getCardId()).flatMap(dc -> {
                    return accountService.findByClientId(dc.getClientId()).flatMap(ac -> {
                        ac.setBalance(ac.getBalance() - transaction.getAmount());
                        if (ac.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance for debit card purchase"));
                        }
                        transaction.setAccountId(ac.getId());
                        transaction.setNewAvailableCredit(ac.getBalance());
                        AccountTransactionDTO accountTransactionDTO = new AccountTransactionDTO(ac.getId(), transaction.getAmount());
                        return accountService.cardPurchase(accountTransactionDTO).flatMap(a -> a.contains("done")
                                        ? databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(seq -> {
                                    transaction.setId(seq);
                                    return transactionRepository.save(transaction)
                                            .flatMap(t -> Mono.just("Purchase done, new available balance: " + ac.getBalance()));
                                })
                                        : Mono.error(new IllegalArgumentException("Error on account transaction"))
                        );
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Debit card not found"))));
    }

    @Override
    public Mono<String> debitDeposit(TransactionDTO transactionDTO) {
        log.info("Debit card deposit: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.DEPOSIT);
        return checkFields(transactionDTO)
                .switchIfEmpty(cardService.findById(transaction.getCardId()).flatMap(dc -> {
                    return accountService.findByClientId(dc.getClientId()).flatMap(ac -> {
                        ac.setBalance(ac.getBalance() + transaction.getAmount());
                        transaction.setAccountId(ac.getId());
                        transaction.setNewAvailableCredit(ac.getBalance());
                        AccountTransactionDTO accountTransactionDTO = new AccountTransactionDTO(ac.getId(), transaction.getAmount());
                        return accountService.cardDeposit(accountTransactionDTO).flatMap(a -> a.contains("done")
                                        ? databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(seq -> {
                                    transaction.setId(seq);
                                    return transactionRepository.save(transaction)
                                            .flatMap(t -> Mono.just("Deposit done, new available balance: " + ac.getBalance()));
                                })
                                        : Mono.error(new IllegalArgumentException("Error on account transaction"))
                        );
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Debit card not found"))));
    }

    @Override
    public Flux<Transaction> findAllByCardId(Long id) {
        log.info("Listing all transactions by card id");
        return transactionRepository.findAllByCardId(id);
    }

    @Override
    public Mono<String> checkFields(TransactionDTO transaction) {
        if (transaction.getDescription() == null || transaction.getDescription().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Credit card transaction description cannot be empty"));
        }
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Credit card transaction amount must be greater than 0"));
        }
        return Mono.empty();
    }

}
