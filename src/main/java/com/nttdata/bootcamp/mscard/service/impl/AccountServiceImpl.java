package com.nttdata.bootcamp.mscard.service.impl;

import com.nttdata.bootcamp.mscard.dto.AccountDTO;
import com.nttdata.bootcamp.mscard.dto.AccountTransactionDTO;
import com.nttdata.bootcamp.mscard.dto.ClientDTO;
import com.nttdata.bootcamp.mscard.dto.TransactionDTO;
import com.nttdata.bootcamp.mscard.service.AccountService;
import com.nttdata.bootcamp.mscard.service.ClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class AccountServiceImpl implements AccountService {

    private final WebClient webClient;

    public AccountServiceImpl(WebClient.Builder webClientBuilder) {
        //microservicio account
        this.webClient = webClientBuilder.baseUrl("http://ms-gateway:8088").build();
    }

    @Override
    public Mono<AccountDTO> findByClientId(Long id) {
        Mono<AccountDTO> accountList = this.webClient.get()
                .uri("/bootcamp/account/find/{id}", id)
                .retrieve()
                .bodyToMono(AccountDTO.class);

        log.info("Account obtained from service ms-account:" + accountList);
        return accountList;
    }

    @Override
    public Flux<AccountDTO> findAllByClientId(Long id) {
        Flux<AccountDTO> accountList = this.webClient.get()
                .uri("/bootcamp/account/findAllByClientId/{id}", id)
                .retrieve()
                .bodyToFlux(AccountDTO.class);

        log.info("Account List obtained from service ms-account:" + accountList);
        return accountList;
    }

    @Override
    public Mono<String> cardPurchase(AccountTransactionDTO transactionDTO) {
        Mono<String> cardPurchase = this.webClient.post()
                .uri("/bootcamp/transaction/cardPurchase")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(transactionDTO), TransactionDTO.class)
                .exchangeToMono(cr -> cr.bodyToMono(String.class))
                .onErrorMap(t -> new RuntimeException("Error in card purchase"));

        log.info("Card purchase done from service ms-account:" + cardPurchase);
        return cardPurchase;
    }

    @CircuitBreaker(name = "service-account", fallbackMethod = "cardDepositFallback")
    @TimeLimiter(name = "service-account")
    @Override
    public Mono<String> cardDeposit(AccountTransactionDTO transactionDTO) {
        return this.webClient.post()
                .uri("/bootcamp/transaction/cardDeposit")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(transactionDTO), TransactionDTO.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .bodyToMono(String.class);
    }

    public Mono<String> cardDepositFallback(AccountTransactionDTO transactionDTO, Throwable t) {
        log.error("Fallback method for cardDeposit (ACCOUNT) executed {}", t.getMessage());
        return Mono.empty();
    }

}
