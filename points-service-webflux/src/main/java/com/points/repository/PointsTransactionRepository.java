package com.points.repository;

import com.points.model.PointsTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends ReactiveCrudRepository<PointsTransaction, Long> {

    Flux<PointsTransaction> findByAccountId(Long accountId);
}
