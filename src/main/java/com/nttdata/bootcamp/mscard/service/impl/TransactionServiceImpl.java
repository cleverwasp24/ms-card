package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.*;
import com.nttdata.bootcamp.mscard.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.mscard.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.mscard.model.Card;
import com.nttdata.bootcamp.mscard.model.Transaction;
import com.nttdata.bootcamp.mscard.model.enums.CardTypeEnum;
import com.nttdata.bootcamp.mscard.model.enums.TransactionTypeEnum;
import com.nttdata.bootcamp.mscard.service.CardService;
import com.nttdata.bootcamp.mscard.service.DatabaseSequenceService;
import com.nttdata.bootcamp.mscard.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

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
                    if (cc.getCardType() == CardTypeEnum.DEBIT.ordinal()) {
                        return Mono.error(new Exception("Card type is not credit"));
                    }
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
                    if (cc.getCardType() == CardTypeEnum.DEBIT.ordinal()) {
                        return Mono.error(new Exception("Card type is not credit"));
                    }
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
                    if (dc.getCardType() == CardTypeEnum.CREDIT.ordinal()) {
                        return Mono.error(new Exception("Card type is not debit"));
                    }
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
                    if (dc.getCardType() == CardTypeEnum.CREDIT.ordinal()) {
                        return Mono.error(new Exception("Card type is not debit"));
                    }
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
    public Flux<Transaction> findAllByCardIdDesc(Long accountId) {
        log.info("Listing all transactions by card id order by date desc");
        return transactionRepository.findAllByCardIdOrderByTransactionDateDesc(accountId);
    }

    @Override
    public Mono<String> checkFields(TransactionDTO transaction) {
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Card transaction amount must be greater than 0"));
        }
        return Mono.empty();
    }

    @Override
    public Flux<Transaction> findTransactionsCardPeriod(Long cardId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findAllByCardIdAndTransactionDateBetween(cardId, start, end);
    }

    @Override
    public Mono<Transaction> findLastTransactionBefore(Long id, LocalDateTime date) {
        return transactionRepository.findByCardIdAndTransactionDateBeforeOrderByTransactionDateDesc(id, date)
                .flatMap(t -> Mono.just(t))
                //if it is empty take the card balance and creation date
                .switchIfEmpty(cardService.findById(id).flatMap(c -> {
                    Transaction transaction = new Transaction();
                    transaction.setNewAvailableCredit(c.getCreditLine());
                    transaction.setTransactionDate(c.getCreationDate());
                    return Mono.just(transaction);
                }));
    }


    @Override
    public Mono<CardReportDTO> generateCardReportCurrentMonth(Long id) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).with(LocalTime.MIN);
        log.info("Generating card report for current month: " + start + " - " + now);
        return generateCardReport(id, new PeriodDTO(start, now));
    }

    @Override
    public Mono<CardReportDTO> generateCardReport(Long id, PeriodDTO periodDTO) {
        log.info("Generating card report in a period: " + periodDTO.getStart() + " - " + periodDTO.getEnd());
        Mono<CardReportDTO> cardReportDTOMono = Mono.just(new CardReportDTO());
        Mono<Card> cardMono = cardService.findById(id);
        Mono<Transaction> firstBefore = findLastTransactionBefore(id, periodDTO.getStart());
        Flux<Transaction> transactionFlux = findTransactionsCardPeriod(id, periodDTO.getStart(), periodDTO.getEnd());
        return cardReportDTOMono.flatMap(r -> cardMono.map(card -> {
                    r.setCard(card);
                    return r;
                }))
                .flatMap(r -> transactionFlux.collectList().map(tl -> {
                    tl = tl.stream().collect(
                                    Collectors.groupingBy(t -> t.getTransactionDate().toLocalDate(),
                                            Collectors.collectingAndThen(
                                                    Collectors.maxBy(
                                                            Comparator.comparing(Transaction::getTransactionDate)),
                                                    transaction -> transaction.get())))
                            .values().stream().collect(Collectors.toList());
                    //Add all transactions to the report as daily balances
                    tl.forEach(t -> r.getDailyBalances().add(new DailyBalanceDTO(t.getTransactionDate().toLocalDate(), t.getNewAvailableCredit())));
                    return r;
                }))
                .flatMap(r -> firstBefore.map(t -> {
                    //If transaction list does not contain a transaction on the start date, add it
                    if (r.getDailyBalances().stream().noneMatch(ta -> ta.getDate().equals(periodDTO.getStart().toLocalDate()))) {
                        if (t.getTransactionDate().toLocalDate().equals(periodDTO.getStart().toLocalDate())) {
                            r.getDailyBalances().add(new DailyBalanceDTO(t.getTransactionDate().toLocalDate(), t.getNewAvailableCredit()));
                        } else {
                            r.getDailyBalances().add(new DailyBalanceDTO(periodDTO.getStart().toLocalDate(), 0.00));
                        }
                    }
                    return r;
                }))
                //Fill missingDays in the transaction list
                .flatMap(r -> {
                    long days = ChronoUnit.DAYS.between(periodDTO.getStart().toLocalDate(), periodDTO.getEnd().toLocalDate());
                    HashMap<LocalDate, Double> map = new HashMap<>();
                    r.getDailyBalances().forEach(t -> map.put(t.getDate(), t.getBalance()));
                    for (int i = 1; i <= days; i++) {
                        LocalDate date = periodDTO.getStart().toLocalDate().plusDays(i);
                        if (!map.containsKey(date)) {
                            map.put(date, map.get(date.minusDays(1)));
                        }
                    }
                    r.setDailyBalances(new ArrayList<>());
                    map.forEach((k, v) -> r.getDailyBalances().add(new DailyBalanceDTO(k, v)));
                    //Sort the list by date
                    r.getDailyBalances().sort(Comparator.comparing(DailyBalanceDTO::getDate));
                    return Mono.just(r);
                });
    }

}
