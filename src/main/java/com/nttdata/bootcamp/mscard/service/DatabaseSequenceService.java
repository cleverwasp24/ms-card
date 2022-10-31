package com.nttdata.bootcamp.mscard.service;

import reactor.core.publisher.Mono;

public interface DatabaseSequenceService {

    Mono<Long> generateSequence(String seqName);

}
