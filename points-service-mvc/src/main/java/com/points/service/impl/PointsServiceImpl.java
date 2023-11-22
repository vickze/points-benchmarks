package com.points.service.impl;

import com.points.model.Account;
import com.points.model.PointsTransaction;
import com.points.repository.AccountRepository;
import com.points.repository.PointsTransactionRepository;
import com.points.request.TransactionRequest;
import com.points.response.GetAccountAndTransactionResponse;
import com.points.response.TransactionResponse;
import com.points.service.PointsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PointsServiceImpl implements PointsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;

    @Transactional
    @Override
    public TransactionResponse transaction(TransactionRequest transactionRequest) {
        TransactionResponse transactionResponse = new TransactionResponse();
        Integer updated;
        if (transactionRequest.getType().equals(1)) {
            updated = accountRepository.addAccountBalance(transactionRequest.getId(), transactionRequest.getAmount());
        } else if (transactionRequest.getType().equals(2)) {
            updated = accountRepository.deductAccountBalance(transactionRequest.getId(), transactionRequest.getAmount());
        } else {
            transactionResponse.setStatus("f");
            transactionResponse.setMsg("invalid type");
            return transactionResponse;
        }
        if (updated == 0) {
            transactionResponse.setStatus("f");
            transactionResponse.setMsg("insufficient balance");
            return transactionResponse;
        }
        Account account = accountRepository.findById(transactionRequest.getId()).get();

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

        pointsTransaction = pointsTransactionRepository.save(pointsTransaction);

        BeanUtils.copyProperties(pointsTransaction, transactionResponse);
        transactionResponse.setId(pointsTransaction.getAccountId());
        transactionResponse.setTransactionId(pointsTransaction.getId());
        transactionResponse.setStatus("s");

        return transactionResponse;
    }


    @Override
    public GetAccountAndTransactionResponse getAccountAndTransaction(Long id) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (!accountOpt.isPresent()) {
            return null;
        }
        Account account = accountOpt.get();
        GetAccountAndTransactionResponse getAccountAndTransactionResponse = new GetAccountAndTransactionResponse();
        BeanUtils.copyProperties(account, getAccountAndTransactionResponse);
        getAccountAndTransactionResponse.setTransactions(pointsTransactionRepository.findByAccountId(id));
        return getAccountAndTransactionResponse;
    }
}
