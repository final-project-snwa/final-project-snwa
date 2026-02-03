package com.team.snwa.snwabackend.domain.order.service;
import com.team.snwa.snwabackend.domain.order.dto.request.OrderCreateRequest;
import com.team.snwa.snwabackend.domain.order.dto.response.OrderCreateResponse;
import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderCreateResponse create(OrderCreateRequest req) {
        String orderId = "ORD-" + UUID.randomUUID();

        Order order = Order.create(
                req.userId(),
                orderId,
                req.orderName(),
                req.amount()
        );

        orderRepository.save(order);

        return new OrderCreateResponse(orderId, req.orderName(), req.amount());

    }
}
