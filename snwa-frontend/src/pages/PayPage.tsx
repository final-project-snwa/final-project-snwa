import { useEffect, useRef, useState } from "react";

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
    const [prepared, setPrepared] = useState<PrepareResponse | null>(null);
    const widgetRef = useRef<any>(null);

    const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string; // Vite 기준

    useEffect(() => {
        // 1) 주문 생성(prepare)
        (async () => {
            const res = await fetch("http://localhost:8080/api/payments/prepare", {
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
    }, []);

    // 2) 토스 위젯 초기화 + 렌더
    const initWidget = async () => {
        if (!prepared) return;
        if (!window.TossPayments) {
            alert("TossPayments 스크립트가 아직 로딩되지 않았어.");
            return;
        }

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

    // prepared가 생기면 자동 init
    useEffect(() => {
        if (prepared) initWidget();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [prepared]);

    const requestPay = async () => {
        if (!prepared || !widgetRef.current) return;

        await widgetRef.current.requestPayment({
            orderId: prepared.orderId,
            orderName: prepared.orderName,
            successUrl: "http://localhost:3000/pay/success",
            failUrl: "http://localhost:3000/pay/fail",
        });
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
