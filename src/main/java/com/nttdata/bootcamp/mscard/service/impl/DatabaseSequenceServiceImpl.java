package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.infrastructure.DatabaseSequenceRepository;
import com.nttdata.bootcamp.mscard.model.DatabaseSequence;
import com.nttdata.bootcamp.mscard.service.DatabaseSequenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class DatabaseSequenceServiceImpl implements DatabaseSequenceService {

    @Autowired
    DatabaseSequenceRepository databaseSequenceRepository;

    @Override
    public Mono<Long> generateSequence(String seq) {
        return databaseSequenceRepository.findDatabaseSequenceById(seq).flatMap(sequence -> {
            sequence.setSeq(sequence.getSeq() + 1);
            return databaseSequenceRepository.save(sequence).flatMap(s -> Mono.just(s.getSeq()));
        }).switchIfEmpty(databaseSequenceRepository.save(new DatabaseSequence(seq, 1L))
                .then(Mono.just(1L)));
    }
}