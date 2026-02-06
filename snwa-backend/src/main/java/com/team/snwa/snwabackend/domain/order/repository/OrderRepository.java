package com.team.snwa.snwabackend.domain.order.repository;

import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderId = :orderId")
    Optional<Order> findByOrderIdForUpdate(@Param("orderId") String orderId);

    @Modifying
    @Transactional
    @Query("update Order o set o.status = :status where o.orderId = :orderId")
    int updateStatus(@Param("orderId") String orderId, @Param("status") OrderStatus status);
}
