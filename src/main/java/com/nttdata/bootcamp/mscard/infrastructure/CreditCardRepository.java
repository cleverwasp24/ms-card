package com.nttdata.bootcamp.mscard.infrastructure;

import com.nttdata.bootcamp.mscard.model.CreditCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard, Integer> {

    Flux<CreditCard> findAllByClientId(Integer id);

}
