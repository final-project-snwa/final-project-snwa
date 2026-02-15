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

const toNumber = (v: string | null): number | null => {
    if (v == null) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
};

export default function PayPage() {
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const [prepared, setPrepared] = useState<OrderCreateResponse | null>(null);
    const [currentPolicyId, setCurrentPolicyId] = useState<number | null>(null);
    const [prepareError, setPrepareError] = useState<string | null>(null);

    const widgetRef = useRef<any>(null);
    const initedRef = useRef(false);

    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string | undefined;

    useEffect(() => {
        const state = location.state as PayLocationState;

        // ✅ 0) URL 쿼리스트링에서도 주문정보 받기
        const qpPolicyId = toNumber(searchParams.get("policyId"));
        const qpOrderId = searchParams.get("orderId");
        const qpOrderName = searchParams.get("orderName");
        const qpAmount = toNumber(searchParams.get("amount"));

        // policyId 결정: state > query > default(1)
        const policyId = state?.policyId ?? qpPolicyId ?? 1;
        setCurrentPolicyId(policyId);

        // ✅ 1) state로 주문정보를 넘겨받았으면 그대로 사용
        if (state?.orderId && state?.orderName != null && state?.amount != null) {
            const next = { orderId: state.orderId, orderName: state.orderName, amount: state.amount };
            setPrepared(next);
            initedRef.current = false; // prepared 바뀌면 위젯 다시 초기화 가능
            return;
        }

        // ✅ 2) 쿼리스트링으로 주문정보가 있으면 그대로 사용
        if (qpOrderId && qpOrderName != null && qpAmount != null) {
            const next = { orderId: qpOrderId, orderName: qpOrderName, amount: qpAmount };
            setPrepared(next);
            initedRef.current = false;
            return;
        }
        let cancelled = false;
        (async () => {
            try {
                const token = sessionStorage.getItem("snwa_token");
                if (!token) {
                    setPrepareError("로그인이 필요합니다. 다시 로그인 후 시도해주세요.");
                    return;
                }

                const res = await fetch("/api/orders", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ policyId }),
                });

                if (!res.ok) {
                    const text = await res.text();
                    console.error("order create failed", res.status, text);
                    setPrepareError(`주문 생성 실패: ${res.status}`);
                    return;
                }

                const data: OrderCreateResponse = await res.json();
                if (cancelled) return;

                setPrepared(data);
                initedRef.current = false;
            } catch (e) {
                console.error("order create exception", e);
                setPrepareError("주문 생성 중 오류가 발생했습니다.");
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [location.state, searchParams]);

    const initWidget = async () => {
        if (!prepared) return;

        if (!clientKey) {
            setPrepareError("VITE_TOSS_CLIENT_KEY가 설정되지 않았습니다.");
            return;
        }
        if (!window.TossPayments) {
            setPrepareError("TossPayments 스크립트가 아직 로딩되지 않았습니다.");
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
            const origin = window.location.origin;
            const successParams = currentPolicyId != null ? `?policyId=${currentPolicyId}` : "";
            await widgetRef.current.requestPayment({
                orderId: prepared.orderId,
                orderName: prepared.orderName,
                successUrl: `${origin}/pay/success${successParams}`,
                failUrl: `${origin}/pay/fail`,
            });
        } catch (e) {
            console.warn("결제창 닫힘/취소/실패", e);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-3xl mx-auto px-4 py-8">
                <h1 className="text-2xl font-bold text-gray-900 mb-6">결제</h1>

                {!prepared && !prepareError && (
                    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                        <p className="text-sm text-gray-500">주문 생성 중...</p>
                    </div>
                )}
                {prepareError && (
                    <div className="bg-white rounded-xl border border-red-200 shadow-sm p-6">
                        <p className="text-sm text-red-600">{prepareError}</p>
                    </div>
                )}

                {prepared && (
                    <>
                        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
                            <h2 className="text-lg font-semibold text-gray-900 mb-4">주문서</h2>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <p className="text-xs text-gray-500">코인</p>
                                    <p className="text-base font-semibold text-gray-900">{prepared.orderName}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-gray-500">결제금액</p>
                                    <p className="text-base font-semibold text-gray-900">
                                        {prepared.amount.toLocaleString()}원
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                            <h2 className="text-lg font-semibold text-gray-900 mb-4">결제 수단</h2>
                            <div id="payment-method" />
                            <div id="agreement" className="mt-4" />

                            <button
                                type="button"
                                className="mt-6 w-full rounded-lg bg-gray-900 px-4 py-3 text-sm font-semibold text-white hover:bg-gray-800 transition-colors"
                                onClick={requestPay}
                            >
                                결제하기
                            </button>
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}