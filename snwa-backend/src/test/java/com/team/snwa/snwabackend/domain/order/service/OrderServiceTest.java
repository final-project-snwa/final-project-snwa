package com.team.snwa.snwabackend.domain.order.service;

import com.team.snwa.snwabackend.domain.order.dto.request.OrderCreateRequest;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.wallet.entity.CoinChargePolicy;
import com.team.snwa.snwabackend.domain.wallet.repository.CoinChargePolicyRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CoinChargePolicyRepository policyRepository;

    @InjectMocks
    OrderService orderService;

    @Test
    void userId가_null이면_UNAUTHORIZED() {
        OrderCreateRequest req = new OrderCreateRequest(1L);

        assertThatThrownBy(() -> orderService.create(null, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
                });

        verifyNoInteractions(policyRepository, orderRepository);
    }

    @Test
    void 정책없으면_POLICY_NOT_FOUND() {
        OrderCreateRequest req = new OrderCreateRequest(999L);
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.POLICY_NOT_FOUND));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 정책비활성_POLICY_INACTIVE() {
        OrderCreateRequest req = new OrderCreateRequest(1L);
        CoinChargePolicy policy = mock(CoinChargePolicy.class);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policy.isActive()).thenReturn(false);

        assertThatThrownBy(() -> orderService.create(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.POLICY_INACTIVE));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 정책가격_이상_POLICY_INVALID_PRICE() {
        OrderCreateRequest req = new OrderCreateRequest(1L);
        CoinChargePolicy policy = mock(CoinChargePolicy.class);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policy.isActive()).thenReturn(true);
        when(policy.getPrice()).thenReturn(0); // 또는 null

        assertThatThrownBy(() -> orderService.create(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.POLICY_INVALID_PRICE));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 성공시_주문저장하고_응답반환() {
        OrderCreateRequest req = new OrderCreateRequest(1L);
        CoinChargePolicy policy = mock(CoinChargePolicy.class);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policy.isActive()).thenReturn(true);
        when(policy.getPrice()).thenReturn(10000);
        when(policy.getCoinAmount()).thenReturn(100);
        when(policy.getName()).thenReturn("100코인 충전");
        when(policy.getId()).thenReturn(1L);

        var res = orderService.create(7L, req);

        assertThat(res.orderId()).startsWith("ORD_");
        assertThat(res.orderName()).isEqualTo("100코인 충전");
        assertThat(res.amount()).isEqualTo(10000L);

        verify(orderRepository).save(argThat(o ->
                o.getUserId().equals(7L)
                        && o.getOrderId().startsWith("ORD_")
                        && o.getAmount().equals(10000L)
                        && o.getCoinAmount().equals(100)
                        && o.getPolicyId().equals(1L)
        ));
    }
}
