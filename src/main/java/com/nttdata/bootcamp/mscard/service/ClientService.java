package com.nttdata.bootcamp.mscard.service;

import com.nttdata.bootcamp.mscard.dto.ClientDTO;
import reactor.core.publisher.Mono;

public interface ClientService {

    Mono<ClientDTO> findById(Long id);

}
