package com.team.snwa.snwabackend.domain.order.controller;

import com.team.snwa.snwabackend.domain.order.dto.request.OrderCreateRequest;
import com.team.snwa.snwabackend.domain.order.dto.response.OrderCreateResponse;
import com.team.snwa.snwabackend.domain.order.service.OrderService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderCreateRequest req
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return orderService.create(user.getId(), req);
    }
}
