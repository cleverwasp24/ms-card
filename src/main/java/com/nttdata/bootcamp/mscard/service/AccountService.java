package com.nttdata.bootcamp.mscard.service;

import com.nttdata.bootcamp.mscard.dto.AccountDTO;
import com.nttdata.bootcamp.mscard.dto.AccountTransactionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<AccountDTO> findByClientId(Long id);
    Flux<AccountDTO> findAllByClientId(Long id);
    Mono<String> cardPurchase(AccountTransactionDTO transactionDTO);
    Mono<String> cardDeposit(AccountTransactionDTO transactionDTO);

}
