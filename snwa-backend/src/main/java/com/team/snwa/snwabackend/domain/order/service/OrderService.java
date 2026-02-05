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

    @Transactional
    public OrderCreateResponse create(OrderCreateRequest req) {
        // ✅ TODO: 로그인 붙이면 여기서 SecurityContext로 userId 가져오기
        Long userId = 1L;

        CoinChargePolicy policy = policyRepository.findById(req.policyId())
                .orElseThrow(() -> new CustomException(ErrorCode.POLICY_NOT_FOUND));

        if (!policy.isActive()) {
            throw new CustomException(ErrorCode.POLICY_INACTIVE);
        }

        String orderId = "ORD_" + UUID.randomUUID().toString().replace("-", "");
        String orderName = policy.getName();          // 예: "10코인"
        int amount = policy.getPrice();              // 예: 1100

        Order order = Order.create(userId, orderId, orderName, amount, policy.getCoinAmount());
        orderRepository.save(order);

        return new OrderCreateResponse(orderId, orderName, amount);
    }
}
