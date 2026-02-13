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
                        결제가 정상적으로 완료되었습니다. 코인 구매 페이지로 이동합니다.
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
    const text = status === "success" ? "완료" : status === "error" ? "실패" : "진행";
    const badgeStyle =
        status === "success"
            ? styles.badgeSuccess
            : status === "error"
                ? styles.badgeError
                : styles.badge;

    return <div style={badgeStyle}>{text}</div>;
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
        background:
            "radial-gradient(1200px 600px at 20% 10%, rgba(99,102,241,0.18), transparent 60%)," +
            "radial-gradient(900px 500px at 80% 20%, rgba(34,197,94,0.14), transparent 55%)," +
            "linear-gradient(180deg, #f8fafc 0%, #f3f4f6 100%)",
    },

    card: {
        width: "min(620px, 100%)",
        background: "rgba(255,255,255,0.86)",
        backdropFilter: "blur(10px)",
        borderRadius: 20,
        padding: 22,
        boxShadow: "0 18px 60px rgba(17,24,39,0.12)",
        border: "1px solid rgba(17,24,39,0.08)",
        position: "relative",
        overflow: "hidden",
    },

    // 카드 상단 포인트 바
    headerAccent: {
        position: "absolute",
        top: 0,
        left: 0,
        right: 0,
        height: 6,
        background:
            "linear-gradient(90deg, rgba(99,102,241,1) 0%, rgba(34,197,94,1) 45%, rgba(59,130,246,1) 100%)",
    },

    headerRow: {
        display: "flex",
        gap: 14,
        alignItems: "center",
        paddingTop: 6, // accent bar 여백
    },

    title: {
        margin: 0,
        fontSize: 22,
        lineHeight: 1.2,
        letterSpacing: -0.2,
        color: "#0f172a",
    },

    sub: {
        margin: "6px 0 0",
        color: "rgba(15,23,42,0.72)",
        fontSize: 14,
        lineHeight: 1.45,
    },

    hr: {
        height: 1,
        background:
            "linear-gradient(90deg, rgba(15,23,42,0.08), rgba(15,23,42,0.02), rgba(15,23,42,0.08))",
        margin: "16px 0",
    },

    bullets: {
        margin: 0,
        paddingLeft: 18,
        color: "#0f172a",
        lineHeight: 1.75,
        fontSize: 14,
    },

    successBox: {
        padding: 14,
        borderRadius: 14,
        background:
            "linear-gradient(180deg, rgba(34,197,94,0.12) 0%, rgba(34,197,94,0.06) 100%)",
        border: "1px solid rgba(34,197,94,0.25)",
        color: "#14532d",
        fontSize: 14,
        boxShadow: "0 8px 20px rgba(34,197,94,0.10)",
    },

    errorBox: {
        padding: 14,
        borderRadius: 14,
        background:
            "linear-gradient(180deg, rgba(239,68,68,0.10) 0%, rgba(239,68,68,0.05) 100%)",
        border: "1px solid rgba(239,68,68,0.25)",
        boxShadow: "0 10px 22px rgba(239,68,68,0.08)",
    },

    errorTitle: {
        fontSize: 13,
        fontWeight: 800,
        marginBottom: 10,
        color: "#7f1d1d",
        letterSpacing: -0.1,
    },

    errorText: {
        margin: 0,
        whiteSpace: "pre-wrap",
        wordBreak: "break-word",
        fontSize: 12.5,
        lineHeight: 1.55,
        color: "#7f1d1d",
        padding: 12,
        borderRadius: 12,
        background: "rgba(255,255,255,0.55)",
        border: "1px solid rgba(127,29,29,0.12)",
    },

    buttonRow: {
        display: "flex",
        gap: 10,
        marginTop: 14,
        flexWrap: "wrap",
    },

    btnPrimary: {
        border: "none",
        background:
            "linear-gradient(180deg, rgba(15,23,42,1) 0%, rgba(17,24,39,1) 100%)",
        color: "white",
        padding: "11px 14px",
        borderRadius: 12,
        cursor: "pointer",
        fontWeight: 800,
        boxShadow: "0 10px 22px rgba(15,23,42,0.18)",
        transition: "transform 120ms ease, box-shadow 120ms ease, opacity 120ms ease",
    },

    btn: {
        border: "1px solid rgba(15,23,42,0.14)",
        background: "rgba(255,255,255,0.9)",
        color: "#0f172a",
        padding: "11px 14px",
        borderRadius: 12,
        cursor: "pointer",
        fontWeight: 700,
        boxShadow: "0 8px 18px rgba(15,23,42,0.06)",
        transition: "transform 120ms ease, box-shadow 120ms ease, opacity 120ms ease",
    },

    help: {
        marginTop: 12,
        color: "rgba(15,23,42,0.68)",
        fontSize: 13,
        lineHeight: 1.55,
    },

    // 기본 배지(loading 외)
    badge: {
        width: 44,
        height: 44,
        borderRadius: 14,
        display: "grid",
        placeItems: "center",
        fontSize: 12,
        fontWeight: 900,
        background: "rgba(15,23,42,0.06)",
        color: "#0f172a",
        border: "1px solid rgba(15,23,42,0.10)",
    },

    badgeSuccess: {
        width: 44,
        height: 44,
        borderRadius: 14,
        display: "grid",
        placeItems: "center",
        fontSize: 12,
        fontWeight: 900,
        background: "rgba(34,197,94,0.12)",
        color: "#14532d",
        border: "1px solid rgba(34,197,94,0.25)",
    },

    badgeError: {
        width: 44,
        height: 44,
        borderRadius: 14,
        display: "grid",
        placeItems: "center",
        fontSize: 12,
        fontWeight: 900,
        background: "rgba(239,68,68,0.12)",
        color: "#7f1d1d",
        border: "1px solid rgba(239,68,68,0.25)",
    },

    spinnerWrap: {
        width: 44,
        height: 44,
        display: "grid",
        placeItems: "center",
        borderRadius: 14,
        background: "rgba(99,102,241,0.08)",
        border: "1px solid rgba(99,102,241,0.18)",
    },

    spinner: {
        width: 26,
        height: 26,
        borderRadius: "50%",
        border: "3px solid rgba(99,102,241,0.18)",
        borderTop: "3px solid rgba(99,102,241,0.85)",
        animation: "spin 0.85s linear infinite",
    },
};