import { useEffect, useMemo, useRef, useState } from "react";
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

const safeDecode = (s: string) => {
    try {
        return decodeURIComponent(s.replace(/\+/g, "%20"));
    } catch {
        return s;
    }
};

export default function PayPage() {
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const [prepared, setPrepared] = useState<OrderCreateResponse | null>(null);
    const [currentPolicyId, setCurrentPolicyId] = useState<number | null>(null);
    const [prepareError, setPrepareError] = useState<string | null>(null);
    const [isPreparing, setIsPreparing] = useState(false);
    const [isPaying, setIsPaying] = useState(false);

    const widgetRef = useRef<any>(null);
    const initedRef = useRef(false);

    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string | undefined;

    // state/query에서 넘어온 값 정리(가능하면 URL decode도)
    const qp = useMemo(() => {
        const qpPolicyId = toNumber(searchParams.get("policyId"));
        const qpOrderId = searchParams.get("orderId");
        const qpOrderName = searchParams.get("orderName");
        const qpAmount = toNumber(searchParams.get("amount"));

        return {
            qpPolicyId,
            qpOrderId: qpOrderId ? safeDecode(qpOrderId) : null,
            qpOrderName: qpOrderName ? safeDecode(qpOrderName) : null,
            qpAmount,
        };
    }, [searchParams]);

    useEffect(() => {
        const state = location.state as PayLocationState;

        // ✅ 에러/상태 초기화
        setPrepareError(null);
        setIsPreparing(false);

        // ✅ policyId 결정: state > query > default(1)
        const policyId = state?.policyId ?? qp.qpPolicyId ?? 1;
        setCurrentPolicyId(policyId);

        // ✅ 1) state로 주문정보를 넘겨받았으면 그대로 사용
        if (state?.orderId && state?.orderName != null && state?.amount != null) {
            const next = { orderId: state.orderId, orderName: state.orderName, amount: state.amount };
            setPrepared(next);
            initedRef.current = false;
            return;
        }

        // ✅ 2) 쿼리스트링으로 주문정보가 있으면 그대로 사용
        if (qp.qpOrderId && qp.qpOrderName != null && qp.qpAmount != null) {
            const next = { orderId: qp.qpOrderId, orderName: qp.qpOrderName, amount: qp.qpAmount };
            setPrepared(next);
            initedRef.current = false;
            return;
        }

        // ✅ 3) 없으면 서버에 주문 생성 요청
        let cancelled = false;
        (async () => {
            try {
                setIsPreparing(true);

                const token = sessionStorage.getItem("snwa_token");
                if (!token) {
                    setPrepareError("로그인이 필요합니다. 다시 로그인 후 시도해 주십시오.");
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

                // 실패 메시지
                if (!res.ok) {
                    let msg = `주문 생성에 실패했습니다. (${res.status})`;
                    const text = await res.text().catch(() => "");
                    // 백엔드가 JSON 형태로 내려주면 message 뽑기
                    try {
                        const j = text ? JSON.parse(text) : null;
                        const m = j?.message || j?.error || j?.code;
                        if (m) msg = `${msg}\n${String(m)}`;
                    } catch {
                        // plain text면 짧게 붙이기
                        if (text) msg = `${msg}\n${text.slice(0, 300)}`;
                    }
                    console.error("order create failed", res.status, text);
                    setPrepareError(msg);
                    return;
                }

                const data: OrderCreateResponse = await res.json();
                if (cancelled) return;

                setPrepared(data);
                initedRef.current = false;
            } catch (e) {
                console.error("order create exception", e);
                setPrepareError("주문 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주십시오.");
            } finally {
                if (!cancelled) setIsPreparing(false);
            }
        })();

        return () => {
            cancelled = true;
        };
        // location.state는 객체 레퍼런스가 흔들릴 수 있어 qp로 안정화 + location.key를 트리거로 사용
    }, [location.key, qp, location.state]);

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
        if (isPaying) return;

        try {
            setIsPaying(true);

            const origin = window.location.origin;
            const successParams = currentPolicyId != null ? `?policyId=${currentPolicyId}` : "";
            const failParams = currentPolicyId != null ? `?policyId=${currentPolicyId}` : "";

            await widgetRef.current.requestPayment({
                orderId: prepared.orderId,
                orderName: prepared.orderName,
                successUrl: `${origin}/pay/success${successParams}`,
                failUrl: `${origin}/pay/fail${failParams}`,
            });
        } catch (e) {
            // 결제창 닫힘/취소/실패는 토스 위젯에서 failUrl로 리다이렉트되지 않을 수 있음(사용자 닫기 등)
            console.warn("결제창 닫힘/취소/실패", e);
        } finally {
            setIsPaying(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-3xl mx-auto px-4 py-8">
                <h1 className="text-2xl font-bold text-gray-900 mb-6">결제</h1>

                {!prepared && !prepareError && (
                    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                        <p className="text-sm text-gray-500">
                            {isPreparing ? "주문 생성 중..." : "주문 정보를 준비하고 있습니다..."}
                        </p>
                    </div>
                )}

                {prepareError && (
                    <div className="bg-white rounded-xl border border-red-200 shadow-sm p-6">
                        <p className="text-sm text-red-600 whitespace-pre-wrap">{prepareError}</p>
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
                                disabled={isPaying}
                                className={[
                                    "mt-6 w-full rounded-lg px-4 py-3 text-sm font-semibold text-white transition-colors",
                                    isPaying ? "bg-gray-400 cursor-not-allowed" : "bg-gray-900 hover:bg-gray-800",
                                ].join(" ")}
                                onClick={requestPay}
                            >
                                {isPaying ? "결제창을 여는 중입니다..." : "결제하기"}
                            </button>

                            <p className="mt-3 text-xs text-gray-500">
                                결제창이 열리지 않으면 팝업 차단 설정을 확인해 주십시오.
                            </p>
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}