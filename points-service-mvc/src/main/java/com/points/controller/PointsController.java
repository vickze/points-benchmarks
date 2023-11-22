package com.points.controller;

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

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PointsController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PointsService pointsService;
    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/plainText")
    public String plainText() {
        return "Hello, World!";
    }

    @GetMapping("/json")
    public Message json() {
        return new Message("Hello, World!");
    }

    @PostMapping("/createAccount")
    public Account createAccount(@RequestBody Account request) {
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        return accountRepository.save(request);
    }

    @PostMapping("/updateAccount")
    public Account updateAccount(@RequestBody Account request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("id can not be null");
        }
        Integer row = accountRepository.updateAccount(request.getId(), request.getName(), LocalDateTime.now());
        if (row == 0) {
            throw new IllegalArgumentException("update failed");
        }
        return request;
    }

    @GetMapping("/getAccount/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    @PostMapping("/transaction")
    public TransactionResponse transaction(@RequestBody TransactionRequest transactionRequest) {
        return pointsService.transaction(transactionRequest);
    }

    @GetMapping("/getAccountAndTransaction/{id}")
    public GetAccountAndTransactionResponse getAccountAndTransaction(@PathVariable Long id) {
        return pointsService.getAccountAndTransaction(id);
    }

}
