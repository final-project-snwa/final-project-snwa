package com.team.snwa.snwabackend.domain.order.service;

import com.team.snwa.snwabackend.domain.order.dto.request.OrderCreateRequest;
import com.team.snwa.snwabackend.domain.order.dto.response.OrderCreateResponse;
import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.wallet.entity.CoinChargePolicy;
import com.team.snwa.snwabackend.domain.wallet.repository.CoinChargePolicyRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CoinChargePolicyRepository policyRepository;

    public OrderCreateResponse create(OrderCreateRequest req) {

        // TODO: SecurityContext 적용
        Long userId = 1L;

        CoinChargePolicy policy = policyRepository.findById(req.policyId())
                .orElseThrow(() -> new CustomException(ErrorCode.POLICY_NOT_FOUND));

        if (!policy.isActive()) {
            throw new CustomException(ErrorCode.POLICY_INACTIVE);
        }

        // ✅ 방어: 정책 데이터 이상치 (선택이지만 추천)
        if (policy.getPrice() == null || policy.getPrice().intValue() <= 0) {
            throw new CustomException(ErrorCode.POLICY_INVALID_PRICE);
        }
        if (policy.getCoinAmount() == null || policy.getCoinAmount() <= 0) {
            throw new CustomException(ErrorCode.POLICY_INVALID_COIN_AMOUNT);
        }
        if (policy.getName() == null || policy.getName().isBlank()) {
            throw new CustomException(ErrorCode.POLICY_INVALID_NAME);
        }

        String orderId = "ORD_" + UUID.randomUUID().toString().replace("-", "");
        String orderName = policy.getName();
        Long amount = policy.getPrice().longValue();

        // ✅ 주문 스냅샷: amount / coinAmount / policyId를 Order에 확정 저장
        Order order = Order.create(
                userId,
                orderId,
                orderName,
                amount,
                policy.getCoinAmount(),
                policy.getId()
        );

        orderRepository.save(order);

        return new OrderCreateResponse(orderId, orderName, amount);
    }
}
