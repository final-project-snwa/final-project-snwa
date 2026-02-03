import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router";

declare global {
    interface Window {
        TossPayments: any;
    }
}

type PrepareResponse = {
    orderId: string;
    orderName: string;
    amount: number;
};

export default function PayPage() {
    const location = useLocation();
    const [prepared, setPrepared] = useState<PrepareResponse | null>(null);

    const widgetRef = useRef<any>(null);

    // ✅ 위젯 중복 초기화 방지 (StrictMode/useEffect 2번 실행 대비)
    const initedRef = useRef(false);

    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string; // Vite 기준

    // 코인 구매 등에서 넘긴 주문 정보가 있으면 사용, 없으면 기존 prepare 호출
    useEffect(() => {
        const state = location.state as { orderId?: string; orderName?: string; amount?: number } | null;
        if (state?.orderId && state?.orderName != null && state?.amount != null) {
            setPrepared({
                orderId: state.orderId,
                orderName: state.orderName,
                amount: state.amount,
            });
            return;
        }

        (async () => {
            const res = await fetch("/api/orders", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: 1,
                    orderName: "기사 1개 열람",
                    amount: 1000,
                }),
            });

            if (!res.ok) {
                console.error("prepare failed", res.status, await res.text());
                return;
            }

            const data: PrepareResponse = await res.json();
            setPrepared(data);
        })();
    }, [location.state]);

    // 2) 토스 위젯 초기화 + 렌더
    const initWidget = async () => {
        if (!prepared) return;

        if (!window.TossPayments) {
            alert("TossPayments 스크립트가 아직 로딩되지 않았어.");
            return;
        }

        // ✅ 혹시 남아있는 렌더 결과가 있으면 비워주기(안전장치)
        const pmEl = document.querySelector("#payment-method");
        const agEl = document.querySelector("#agreement");
        if (pmEl) pmEl.innerHTML = "";
        if (agEl) agEl.innerHTML = "";

        const tossPayments = window.TossPayments(clientKey);

        // 비회원이면 ANONYMOUS
        const widgets = tossPayments.widgets({ customerKey: "ANONYMOUS" });

        await widgets.setAmount({ currency: "KRW", value: prepared.amount });

        await Promise.all([
            widgets.renderPaymentMethods({
                selector: "#payment-method",
                variantKey: "DEFAULT",
            }),
            widgets.renderAgreement({
                selector: "#agreement",
                variantKey: "AGREEMENT",
            }),
        ]);

        widgetRef.current = widgets;
    };

    // prepared가 생기면 자동 init (✅ 1회만 실행)
    useEffect(() => {
        if (!prepared) return;

        if (initedRef.current) return; // ✅ 중복 방지
        initedRef.current = true;

        initWidget().catch((e) => {
            console.error("initWidget failed", e);
            initedRef.current = false; // 초기화 실패하면 다시 시도 가능하게
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
            // ✅ 결제창 닫기/취소는 흔한 정상 흐름이라 에러로 죽이면 안 됨
            console.warn("결제창 닫힘/취소/실패", e);
        }
    };

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 위젯 테스트 (React)</h1>

            {!prepared && <p>주문 생성 중...</p>}

            {prepared && (
                <>
                    <p>orderId: {prepared.orderId}</p>
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
