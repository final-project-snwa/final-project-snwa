package com.team.snwa.snwabackend.domain.order.controller;

import com.team.snwa.snwabackend.domain.order.dto.request.OrderCreateRequest;
import com.team.snwa.snwabackend.domain.order.dto.response.OrderCreateResponse;
import com.team.snwa.snwabackend.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse create(@Valid @RequestBody OrderCreateRequest req) {
        return orderService.create(req);
    }
}