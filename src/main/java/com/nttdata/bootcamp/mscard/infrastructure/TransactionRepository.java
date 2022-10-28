package com.nttdata.bootcamp.mscard.infrastructure;

import com.nttdata.bootcamp.mscard.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, Integer> {

    Flux<Transaction> findAllByCreditCardId(Integer id);
}
