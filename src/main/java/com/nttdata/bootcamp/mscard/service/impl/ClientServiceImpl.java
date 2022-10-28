package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.ClientDTO;
import com.nttdata.bootcamp.mscard.service.ClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class ClientServiceImpl implements ClientService {

    private final WebClient webClient;

    public ClientServiceImpl(WebClient.Builder webClientBuilder) {
        //microservicio client
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public Mono<ClientDTO> findById(Integer id) {
        Mono<ClientDTO> clientById =  this.webClient.get()
                .uri("/bootcamp/client/find/{id}", id)
                .retrieve()
                .bodyToMono(ClientDTO.class);

        log.info("Client obtained from service ms-client:" + clientById);
        return clientById;
    }

}
