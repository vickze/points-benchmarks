package com.points.repository;

import com.points.model.Account;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {

    @Modifying
    @Query("UPDATE account SET name = :name, update_time = :updateTime WHERE id = :id")
    Mono<Integer> updateAccount(@Param("id") Long id, @Param("name") String name, @Param("updateTime") LocalDateTime updateTime);

    @Modifying
    @Query("UPDATE account SET balance = balance + :amount WHERE id = :id")
    Mono<Integer> addAccountBalance(@Param("id") Long id, @Param("amount") Long amount);

    @Modifying
    @Query("UPDATE account SET balance = balance - :amount WHERE id = :id AND balance >= :amount")
    Mono<Integer> deductAccountBalance(@Param("id") Long id, @Param("amount") Long amount);

}
