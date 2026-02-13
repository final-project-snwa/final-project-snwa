import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router";

type Status = "loading" | "success" | "error";

export default function PaySuccessPage() {
    const [sp] = useSearchParams();
    const navigate = useNavigate();

    const [status, setStatus] = useState<Status>("loading");
    const [step, setStep] = useState("결제 정보를 확인하고 있습니다.");
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        let alive = true;

        (async () => {
            try {
                setStatus("loading");
                setErr(null);

                const paymentKey = sp.get("paymentKey");
                const orderId = sp.get("orderId");
                const amount = sp.get("amount");
                const policyId = sp.get("policyId");

                if (!paymentKey || !orderId || !amount) {
                    throw new Error("successUrl 파라미터가 누락되었습니다. (paymentKey/orderId/amount)");
                }

                setStep("로그인 상태를 확인하고 있습니다.");
                const token = sessionStorage.getItem("snwa_token");
                if (!token) {
                    throw new Error("로그인이 필요합니다. 다시 로그인해 주십시오.");
                }

                setStep("결제 승인을 요청하고 있습니다.");
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
                    const msg =
                        body?.message ||
                        body?.error ||
                        body?.code ||
                        (body ? JSON.stringify(body) : "") ||
                        "결제 승인에 실패했습니다.";
                    throw new Error(`${res.status} ${msg}`);
                }

                setStep("승인 처리 후 정리 작업을 진행하고 있습니다.");
                if (policyId) {
                    sessionStorage.removeItem(`pay_prepared_policy_${policyId}`);
                }

                if (!alive) return;

                setStatus("success");
                setStep("결제가 완료되었습니다. 잠시 후 이동합니다.");

                setTimeout(() => {
                    navigate("/coins", { replace: true });
                }, 600);
            } catch (e: any) {
                if (!alive) return;
                setStatus("error");
                setStep("결제 처리에 실패했습니다.");
                setErr(e?.message ?? "알 수 없는 오류가 발생했습니다.");
            }
        })();

        return () => {
            alive = false;
        };
    }, [sp, navigate]);

    const onRetry = () => {
        window.location.reload();
    };

    const goHome = () => navigate("/", { replace: true });
    const goLogin = () => navigate("/login", { replace: true });

    return (
        <div style={styles.page}>
            <div style={styles.card}>
                <div style={styles.headerRow}>
                    {status === "loading" && <Spinner />}
                    {status !== "loading" && <StatusBadge status={status} />}
                    <div>
                        <h1 style={styles.title}>
                            {status === "loading" && "결제 처리 중입니다"}
                            {status === "success" && "결제가 완료되었습니다"}
                            {status === "error" && "결제 처리에 실패했습니다"}
                        </h1>
                        <p style={styles.sub}>{step}</p>
                    </div>
                </div>

                <div style={styles.hr} />

                {status === "loading" && (
                    <ul style={styles.bullets}>
                        <li>잠시만 기다려 주십시오.</li>
                        <li>네트워크 상황에 따라 처리 시간이 소요될 수 있습니다.</li>
                        <li>창을 닫지 말고 기다려 주시기 바랍니다.</li>
                    </ul>
                )}

                {status === "success" && (
                    <div style={styles.successBox}>
                        결제가 정상적으로 완료되었습니다. 코인 페이지로 이동합니다.
                    </div>
                )}

                {status === "error" && (
                    <div>
                        <div style={styles.errorBox}>
                            <div style={styles.errorTitle}>오류 내용</div>
                            <pre style={styles.errorText}>{err}</pre>
                        </div>

                        <div style={styles.buttonRow}>
                            <button style={styles.btnPrimary} onClick={onRetry}>
                                다시 시도
                            </button>
                            <button style={styles.btn} onClick={goHome}>
                                홈으로 이동
                            </button>
                            <button style={styles.btn} onClick={goLogin}>
                                로그인 페이지로 이동
                            </button>
                        </div>

                        <p style={styles.help}>
                            문제가 지속될 경우 결제 내역을 확인하시거나 고객센터로 문의해 주시기 바랍니다.
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
}

function StatusBadge({ status }: { status: Status }) {
    const text =
        status === "success" ? "완료" : status === "error" ? "실패" : "진행";
    return <div style={styles.badge}>{text}</div>;
}

function Spinner() {
    return (
        <div style={styles.spinnerWrap} aria-label="loading">
            <div style={styles.spinner} />
            <style>
                {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
            </style>
        </div>
    );
}

const styles: Record<string, React.CSSProperties> = {
    page: {
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 24,
        background: "#f6f7f9",
    },
    card: {
        width: "min(560px, 100%)",
        background: "white",
        borderRadius: 16,
        padding: 20,
        boxShadow: "0 10px 30px rgba(0,0,0,0.08)",
        border: "1px solid rgba(0,0,0,0.06)",
    },
    headerRow: { display: "flex", gap: 14, alignItems: "center" },
    title: { margin: 0, fontSize: 22, lineHeight: 1.2 },
    sub: { margin: "6px 0 0", color: "#555", fontSize: 14 },
    hr: { height: 1, background: "rgba(0,0,0,0.08)", margin: "16px 0" },

    bullets: { margin: 0, paddingLeft: 18, color: "#333", lineHeight: 1.6 },

    successBox: {
        padding: 12,
        borderRadius: 12,
        background: "#f0fff4",
        border: "1px solid #c6f6d5",
        color: "#22543d",
        fontSize: 14,
    },

    errorBox: {
        padding: 12,
        borderRadius: 12,
        background: "#fff5f5",
        border: "1px solid #fed7d7",
    },
    errorTitle: { fontSize: 13, fontWeight: 700, marginBottom: 8, color: "#742a2a" },
    errorText: {
        margin: 0,
        whiteSpace: "pre-wrap",
        wordBreak: "break-word",
        fontSize: 12,
        color: "#742a2a",
    },
    buttonRow: { display: "flex", gap: 8, marginTop: 12, flexWrap: "wrap" },
    btnPrimary: {
        border: "none",
        background: "#111827",
        color: "white",
        padding: "10px 12px",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: 700,
    },
    btn: {
        border: "1px solid rgba(0,0,0,0.15)",
        background: "white",
        color: "#111827",
        padding: "10px 12px",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: 600,
    },
    help: { marginTop: 10, color: "#555", fontSize: 13, lineHeight: 1.5 },

    badge: {
        width: 42,
        height: 42,
        borderRadius: 12,
        display: "grid",
        placeItems: "center",
        fontSize: 12,
        fontWeight: 800,
        background: "rgba(0,0,0,0.06)",
        color: "#111827",
    },

    spinnerWrap: { width: 42, height: 42, display: "grid", placeItems: "center" },
    spinner: {
        width: 28,
        height: 28,
        borderRadius: "50%",
        border: "3px solid rgba(0,0,0,0.12)",
        borderTop: "3px solid rgba(0,0,0,0.6)",
        animation: "spin 0.9s linear infinite",
    },
};
