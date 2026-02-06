package com.team.snwa.snwabackend.domain.order.service;

import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderRepository orderRepository;

    // ✅ 코인 지급 실패: 락 없이 빠르게 상태만 찍기
    public void markChargeFailedFast(String orderId) {
        orderRepository.updateStatus(orderId, OrderStatus.CHARGE_FAILED);
    }

    public void markRefundFailedFast(String orderId) {
        orderRepository.updateStatus(orderId, OrderStatus.REFUND_FAILED);
    }

    public void markCancelCompletedFast(String orderId) {
        orderRepository.updateStatus(orderId, OrderStatus.CANCEL_COMPLETED);
    }

    // ✅ FAILED 같은 건 confirm 커밋 트랜잭션 안에서 "order 엔티티 직접" 처리하는 게 더 안전
}

