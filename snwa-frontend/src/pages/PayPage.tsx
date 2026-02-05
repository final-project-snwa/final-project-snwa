import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router";

declare global {
    interface Window {
        TossPayments: any;
    }
}

type OrderCreateResponse = {
    orderId: string;
    orderName: string;
    amount: number;
};

export default function PayPage() {
    const location = useLocation();
    const [prepared, setPrepared] = useState<OrderCreateResponse | null>(null);

    const widgetRef = useRef<any>(null);
    const orderCreatedRef = useRef(false);
    const initedRef = useRef(false);

    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string;

    useEffect(() => {
        const state = location.state as
            | { policyId?: number; orderId?: string; orderName?: string; amount?: number }
            | null;

        // 이미 주문정보를 넘겨받았으면 그대로 사용
        if (state?.orderId && state?.orderName != null && state?.amount != null) {
            setPrepared({ orderId: state.orderId, orderName: state.orderName, amount: state.amount });
            return;
        }

        if (orderCreatedRef.current) return;
        orderCreatedRef.current = true;

        const policyId = state?.policyId ?? 1; // 기본 10코인

        (async () => {
            const res = await fetch("/api/orders", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // 로그인 붙이면 Authorization 추가
                    // "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({ policyId }),
            });

            if (!res.ok) {
                console.error("order create failed", res.status, await res.text());
                orderCreatedRef.current = false;
                return;
            }

            const data: OrderCreateResponse = await res.json();
            setPrepared(data);
        })();
    }, [location.state]);

    const initWidget = async () => {
        if (!prepared) return;

        if (!window.TossPayments) {
            alert("TossPayments 스크립트가 아직 로딩되지 않았어.");
            return;
        }

        const pmEl = document.querySelector("#payment-method");
        const agEl = document.querySelector("#agreement");
        if (pmEl) pmEl.innerHTML = "";
        if (agEl) agEl.innerHTML = "";

        const tossPayments = window.TossPayments(clientKey);
        const widgets = tossPayments.widgets({ customerKey: "ANONYMOUS" });

        await widgets.setAmount({ currency: "KRW", value: prepared.amount });

        await Promise.all([
            widgets.renderPaymentMethods({ selector: "#payment-method", variantKey: "DEFAULT" }),
            widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
        ]);

        widgetRef.current = widgets;
    };

    useEffect(() => {
        if (!prepared) return;
        if (initedRef.current) return;

        initedRef.current = true;
        initWidget().catch((e) => {
            console.error("initWidget failed", e);
            initedRef.current = false;
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [prepared]);

    const requestPay = async () => {
        if (!prepared || !widgetRef.current) return;

        try {
            await widgetRef.current.requestPayment({
                orderId: prepared.orderId,
                orderName: prepared.orderName,
                successUrl: "http://localhost:3000/pay/success",
                failUrl: "http://localhost:3000/pay/fail",
            });
        } catch (e) {
            console.warn("결제창 닫힘/취소/실패", e);
        }
    };

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 위젯 테스트</h1>

            {!prepared && <p>주문 생성 중...</p>}

            {prepared && (
                <>
                    <p>orderId: {prepared.orderId}</p>
                    <p>orderName: {prepared.orderName}</p>
                    <p>amount: {prepared.amount}</p>

                    <div id="payment-method" style={{ marginTop: 16 }} />
                    <div id="agreement" style={{ marginTop: 16 }} />

                    <button style={{ marginTop: 16 }} onClick={requestPay}>
                        결제하기
                    </button>
                </>
            )}
        </div>
    );
}
