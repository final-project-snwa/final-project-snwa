import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router";

export default function PaySuccessPage() {
    const [sp] = useSearchParams();
    const [data, setData] = useState<any>(null);
    const [err, setErr] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        (async () => {
            const paymentKey = sp.get("paymentKey");
            const orderId = sp.get("orderId");
            const amount = sp.get("amount");
            const policyId = sp.get("policyId");

            if (!paymentKey || !orderId || !amount) {
                setErr("successUrl 파라미터 누락");
                return;
            }

            const token = sessionStorage.getItem("snwa_token");
            if (!token) {
                setErr("로그인이 필요합니다. 다시 로그인해주세요.");
                return;
            }

            const res = await fetch("/api/payments/confirm", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
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

            if (policyId) {
                sessionStorage.removeItem(`pay_prepared_policy_${policyId}`);
            }
            setData(body);
            navigate("/coins", { replace: true });
        })();
    }, [sp, navigate]);

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 처리 중...</h1>
            {err && <pre>{err}</pre>}
            {!err && !data && <p>승인 처리중...</p>}
        </div>
    );
}
