import { useEffect, useRef, useState } from "react";
import { useLocation, useSearchParams } from "react-router";

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

type PayLocationState =
    | { policyId?: number; orderId?: string; orderName?: string; amount?: number }
    | null;

const storageKey = (policyId: number) => `pay_prepared_policy_${policyId}`;

const toNumber = (v: string | null): number | null => {
    if (v == null) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
};

export default function PayPage() {
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const [prepared, setPrepared] = useState<OrderCreateResponse | null>(null);

    const widgetRef = useRef<any>(null);
    const initedRef = useRef(false);

    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string;

    useEffect(() => {
        const state = location.state as PayLocationState;

        // ✅ 0) URL 쿼리스트링에서도 주문정보 받기
        const qpPolicyId = toNumber(searchParams.get("policyId"));
        const qpOrderId = searchParams.get("orderId");
        const qpOrderName = searchParams.get("orderName");
        const qpAmount = toNumber(searchParams.get("amount"));

        // policyId 결정: state > query > default(1)
        const policyId = state?.policyId ?? qpPolicyId ?? 1;

        // ✅ 1) state로 주문정보를 넘겨받았으면 그대로 사용 + 캐시 갱신
        if (state?.orderId && state?.orderName != null && state?.amount != null) {
            const next = { orderId: state.orderId, orderName: state.orderName, amount: state.amount };
            setPrepared(next);
            sessionStorage.setItem(storageKey(policyId), JSON.stringify(next));
            initedRef.current = false; // prepared 바뀌면 위젯 다시 초기화 가능
            return;
        }

        // ✅ 2) 쿼리스트링으로 주문정보가 있으면 그대로 사용 + 캐시 갱신
        if (qpOrderId && qpOrderName != null && qpAmount != null) {
            const next = { orderId: qpOrderId, orderName: qpOrderName, amount: qpAmount };
            setPrepared(next);
            sessionStorage.setItem(storageKey(policyId), JSON.stringify(next));
            initedRef.current = false;
            return;
        }

        // ✅ 3) 캐시가 있으면 재사용
        const cachedRaw = sessionStorage.getItem(storageKey(policyId));
        if (cachedRaw) {
            try {
                const cached = JSON.parse(cachedRaw) as OrderCreateResponse;
                if (cached?.orderId && cached?.orderName != null && cached?.amount != null) {
                    setPrepared(cached);
                    initedRef.current = false;
                    return;
                }
            } catch {
                sessionStorage.removeItem(storageKey(policyId));
            }
        }

        // ✅ 4) 캐시도 없으면 주문 생성
        let cancelled = false;
        (async () => {
            try {
                const res = await fetch("/api/orders", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ policyId }),
                });

                if (!res.ok) {
                    console.error("order create failed", res.status, await res.text());
                    return;
                }

                const data: OrderCreateResponse = await res.json();
                if (cancelled) return;

                setPrepared(data);
                sessionStorage.setItem(storageKey(policyId), JSON.stringify(data));
                initedRef.current = false;
            } catch (e) {
                console.error("order create exception", e);
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [location.state, searchParams]);

    const initWidget = async () => {
        if (!prepared) return;

        if (!window.TossPayments) {
            alert("TossPayments 스크립트가 아직 로딩되지 않았어.");
            return;
        }

        // ✅ 위젯 DOM 초기화
        const pmEl = document.querySelector("#payment-method");
        const agEl = document.querySelector("#agreement");
        if (pmEl) (pmEl as HTMLElement).innerHTML = "";
        if (agEl) (agEl as HTMLElement).innerHTML = "";

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
    }, [prepared]);

    const requestPay = async () => {
        if (!prepared || !widgetRef.current) return;

        try {
            await widgetRef.current.requestPayment({
                orderId: prepared.orderId,
                orderName: prepared.orderName,
                successUrl: `http://localhost:3000/pay/success`,
                failUrl: `http://localhost:3000/pay/fail`,
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
