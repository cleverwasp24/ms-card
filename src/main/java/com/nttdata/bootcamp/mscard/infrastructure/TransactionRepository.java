package com.nttdata.bootcamp.mscard.infrastructure;

import com.nttdata.bootcamp.mscard.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, Long> {

    Flux<Transaction> findAllByCardId(Long id);

    Flux<Transaction> findAllByCardIdOrderByTransactionDateDesc(Long accountId);

    Flux<Transaction> findAllByCardIdAndTransactionDateBetween(Long cardId, LocalDateTime start, LocalDateTime end);

    Mono<Transaction> findByCardIdAndTransactionDateBeforeOrderByTransactionDateDesc(Long cardId, LocalDateTime date);
}
