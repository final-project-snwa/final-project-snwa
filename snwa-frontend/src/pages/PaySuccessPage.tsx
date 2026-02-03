import { useEffect, useState } from "react";
import { useSearchParams } from "react-router";

export default function PaySuccessPage() {
    const [sp] = useSearchParams();
    const [data, setData] = useState<any>(null);
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            const paymentKey = sp.get("paymentKey");
            const orderId = sp.get("orderId");
            const amount = sp.get("amount");

            if (!paymentKey || !orderId || !amount) {
                setErr("successUrl 파라미터 누락");
                return;
            }

            const res = await fetch("http://localhost:8080/api/payments/confirm", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    paymentKey,
                    orderId,
                    amount: Number(amount),
                }),
            });

            const body = await res.json().catch(() => null);
            if (!res.ok) {
                setErr(`${res.status} ${JSON.stringify(body)}`);
                return;
            }

            setData(body);
        })();
    }, [sp]);

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 성공</h1>
            {err && <pre>{err}</pre>}
            {data && <pre>{JSON.stringify(data, null, 2)}</pre>}
            {!err && !data && <p>승인 처리중...</p>}
        </div>
    );
}
