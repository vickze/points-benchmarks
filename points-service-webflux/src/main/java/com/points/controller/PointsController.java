package com.points.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.points.model.Account;
import com.points.model.Message;
import com.points.repository.AccountRepository;
import com.points.request.TransactionRequest;
import com.points.response.GetAccountAndTransactionResponse;
import com.points.response.TransactionResponse;
import com.points.service.PointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class PointsController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PointsService pointsService;
    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/plainText")
    public Mono<String> plainText() {
        return Mono.just("Hello, World!");
    }

    @GetMapping("/json")
    public Mono<Message> json() {
        LOG.info("threader");
        return Mono.just(new Message("Hello, World!"));
    }

    @PostMapping("/createAccount")
    public Mono<Account> createAccount(@RequestBody Account request) throws JsonProcessingException {
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        Mono<Account> response = accountRepository.save(request);
        return response;
    }

    @PostMapping("/updateAccount")
    public Mono<Account> updateAccount(@RequestBody Account request) throws JsonProcessingException {
        if (request.getId() == null) {
            throw new IllegalArgumentException("id can not be null");
        }

        Mono<Integer> row = accountRepository.updateAccount(request.getId(), request.getName(), LocalDateTime.now());
        return row.flatMap(integer -> {
            if (integer == 0) {
                throw new IllegalArgumentException("update failed");
            }
            return Mono.just(request);
        });
    }

    @GetMapping("/getAccount/{id}")
    public Mono<Account> getAccount(@PathVariable Long id) throws JsonProcessingException {
        return accountRepository.findById(id);
    }

    @PostMapping("/transaction")
    public Mono<TransactionResponse> transaction(@RequestBody TransactionRequest transactionRequest) throws JsonProcessingException {
        return pointsService.transaction(transactionRequest);
    }

    @GetMapping("/getAccountAndTransaction/{id}")
    public Mono<GetAccountAndTransactionResponse> getAccountAndTransaction(@PathVariable Long id) throws JsonProcessingException {
        return pointsService.getAccountAndTransaction(id);
    }

}
