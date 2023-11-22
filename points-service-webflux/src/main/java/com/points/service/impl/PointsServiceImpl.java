package com.points.service.impl;

import com.points.model.PointsTransaction;
import com.points.repository.AccountRepository;
import com.points.repository.PointsTransactionRepository;
import com.points.request.TransactionRequest;
import com.points.response.GetAccountAndTransactionResponse;
import com.points.response.TransactionResponse;
import com.points.service.PointsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PointsServiceImpl implements PointsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;


    @Autowired
    private ReactiveTransactionManager reactiveTransactionManager;

    @Transactional
    @Override
    public Mono<TransactionResponse> transaction(TransactionRequest transactionRequest) {
        TransactionResponse transactionResponse = new TransactionResponse();
        Mono<Integer> updated;
        if (transactionRequest.getType().equals(1)) {
            updated = accountRepository.addAccountBalance(transactionRequest.getId(), transactionRequest.getAmount());
        } else if (transactionRequest.getType().equals(2)) {
            updated = accountRepository.deductAccountBalance(transactionRequest.getId(), transactionRequest.getAmount());
        } else {
            transactionResponse.setStatus("f");
            transactionResponse.setMsg("invalid type");
            return Mono.just(transactionResponse);
        }



        return updated.flatMap(integer -> {
            if (integer == 0) {
                transactionResponse.setStatus("f");
                transactionResponse.setMsg("insufficient balance");
                return Mono.justOrEmpty(Optional.empty());
            }
            return accountRepository.findById(transactionRequest.getId());
        }).flatMap(account -> {
            long afterBalance = account.getBalance();
            long previousBalance;

            if (transactionRequest.getType().equals(1)) {
                previousBalance = afterBalance - transactionRequest.getAmount();
            } else {
                previousBalance = afterBalance + transactionRequest.getAmount();
            }

            PointsTransaction pointsTransaction = new PointsTransaction();
            BeanUtils.copyProperties(transactionRequest, pointsTransaction);
            pointsTransaction.setId(null);
            pointsTransaction.setAccountId(transactionRequest.getId());
            pointsTransaction.setPreviousBalance(previousBalance);
            pointsTransaction.setAfterBalance(afterBalance);
            pointsTransaction.setCreateTime(LocalDateTime.now());

            return pointsTransactionRepository.save(pointsTransaction);
        }).flatMap(pointsTransaction -> {
            BeanUtils.copyProperties(pointsTransaction, transactionResponse);
            transactionResponse.setId(pointsTransaction.getAccountId());
            transactionResponse.setTransactionId(pointsTransaction.getId());
            transactionResponse.setStatus("s");

            return Mono.just(transactionResponse);
        }).doOnError(throwable -> {
            throw new RuntimeException(throwable);
        });
    }


    @Override
    public Mono<GetAccountAndTransactionResponse> getAccountAndTransaction(Long id) {
        GetAccountAndTransactionResponse getAccountAndTransactionResponse = new GetAccountAndTransactionResponse();
        return accountRepository.findById(id).flatMap(account -> {
            if (account == null) {
                return Mono.justOrEmpty(Optional.empty());
            }
            BeanUtils.copyProperties(account, getAccountAndTransactionResponse);
            return pointsTransactionRepository.findByAccountId(id).collectList();
        }).flatMap(pointsTransactions -> {
            getAccountAndTransactionResponse.setTransactions(pointsTransactions);
            return Mono.just(getAccountAndTransactionResponse);
        });
    }
}
