package com.team.snwa.snwabackend.domain.order.controller;

import com.team.snwa.snwabackend.domain.order.dto.request.OrderCreateRequest;
import com.team.snwa.snwabackend.domain.order.dto.response.OrderCreateResponse;
import com.team.snwa.snwabackend.domain.order.service.OrderService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse create(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody OrderCreateRequest req
    ) {
        if (email == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return orderService.create(user.getId(), req);
    }
}
