package com.points.service;

import com.points.request.TransactionRequest;
import com.points.response.GetAccountAndTransactionResponse;
import com.points.response.TransactionResponse;
import reactor.core.publisher.Mono;

public interface PointsService {

    Mono<TransactionResponse> transaction(TransactionRequest transactionRequest);

    Mono<GetAccountAndTransactionResponse> getAccountAndTransaction(Long id);

}
