package com.nttdata.bootcamp.mscard.infrastructure;

import com.nttdata.bootcamp.mscard.model.Card;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CardRepository extends ReactiveMongoRepository<Card, Long> {

    Flux<Card> findAllByClientId(Long id);

    Flux<Card> findAllByClientIdAndCardType(Long id, Integer cardType);

}
