package com.points.repository;

import com.points.model.PointsTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends CrudRepository<PointsTransaction, Long> {

    List<PointsTransaction> findByAccountId(Long accountId);
}
