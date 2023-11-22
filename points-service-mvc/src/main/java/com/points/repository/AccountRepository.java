package com.points.repository;

import com.points.model.Account;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    @Modifying
    @Query("UPDATE account SET name = :name, update_time = :updateTime WHERE id = :id")
    Integer updateAccount(@Param("id") Long id, @Param("name") String name, @Param("updateTime") LocalDateTime updateTime);

    @Modifying
    @Query("UPDATE account SET balance = balance + :amount WHERE id = :id")
    Integer addAccountBalance(@Param("id") Long id, @Param("amount") Long amount);

    @Modifying
    @Query("UPDATE account SET balance = balance - :amount WHERE id = :id AND balance >= :amount")
    Integer deductAccountBalance(@Param("id") Long id, @Param("amount") Long amount);

}
