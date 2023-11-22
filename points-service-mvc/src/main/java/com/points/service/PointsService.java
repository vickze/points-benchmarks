package com.points.service;

import com.points.request.TransactionRequest;
import com.points.response.GetAccountAndTransactionResponse;
import com.points.response.TransactionResponse;

public interface PointsService {

    TransactionResponse transaction(TransactionRequest transactionRequest);

    GetAccountAndTransactionResponse getAccountAndTransaction(Long id);

}
