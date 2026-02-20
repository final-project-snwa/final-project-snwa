import { useMemo } from "react";
import { useNavigate, useSearchParams } from "react-router";

export default function PayFailPage() {
    const [sp] = useSearchParams();
    const navigate = useNavigate();

    const code = sp.get("code") || sp.get("errorCode");
    const message = sp.get("message") || sp.get("errorMessage") || sp.get("error");
    const orderId = sp.get("orderId");
    const amount = sp.get("amount");

    const detail = useMemo(() => {
        // Toss에서 message가 url-encoded로 오는 경우가 있어서 안전하게 처리
        const safeMsg = message ? safeDecode(message) : null;
        return {
            code,
            message: safeMsg,
            orderId,
            amount,
        };
    }, [code, message, orderId, amount]);

    const onRetry = () => window.location.reload();
    const goHome = () => navigate("/", { replace: true });
    const goCoins = () => navigate("/coins", { replace: true });

    return (
        <div style={styles.page}>
            <div style={styles.card}>
                <div style={styles.headerAccent} />

                <div style={styles.headerRow}>
                    <StatusBadge />
                    <div>
                        <h1 style={styles.title}>결제가 실패했습니다</h1>
                        <p style={styles.sub}>
                            결제 진행 중 문제가 발생했습니다. 아래 내용을 확인하시고 다시 시도해 주십시오.
                        </p>
                    </div>
                </div>

                <div style={styles.hr} />

                <div style={styles.errorBox}>
                    <div style={styles.errorTitle}>오류 내용</div>

                    <div style={styles.kvGrid}>
                        <KeyValue label="오류 코드" value={detail.code} />
                        <KeyValue label="주문 번호" value={detail.orderId} />
                        <KeyValue label="결제 금액" value={detail.amount ? `${detail.amount}원` : null} />
                    </div>

                    <pre style={styles.errorText}>
                        {detail.message ?? "알 수 없는 오류가 발생했습니다."}
                    </pre>

                    {/* 디버깅이 필요하면 이거 켜서 확인용으로 써 */}
                    {/* <pre style={{ ...styles.errorText, marginTop: 10 }}>{sp.toString()}</pre> */}
                </div>

                <div style={styles.buttonRow}>
                    <button style={styles.btnPrimary} onClick={onRetry}>
                        다시 시도
                    </button>
                    <button style={styles.btn} onClick={goCoins}>
                        코인 페이지로 이동
                    </button>
                    <button style={styles.btn} onClick={goHome}>
                        홈으로 이동
                    </button>
                </div>

                <p style={styles.help}>
                    문제가 지속될 경우 결제 내역 또는 주문 번호를 확인하신 뒤 고객센터로 문의해 주시기 바랍니다.
                </p>
            </div>
        </div>
    );
}

function StatusBadge() {
    return <div style={styles.badgeError}>실패</div>;
}

function KeyValue({ label, value }: { label: string; value: string | null | undefined }) {
    return (
        <div style={styles.kvItem}>
            <div style={styles.kvLabel}>{label}</div>
            <div style={styles.kvValue}>{value ?? "-"}</div>
        </div>
    );
}

function safeDecode(s: string) {
    try {
        return decodeURIComponent(s.replace(/\+/g, "%20"));
    } catch {
        return s;
    }
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

    headerAccent: {
        position: "absolute",
        top: 0,
        left: 0,
        right: 0,
        height: 6,
        background:
            "linear-gradient(90deg, rgba(99,102,241,1) 0%, rgba(239,68,68,1) 50%, rgba(59,130,246,1) 100%)",
    },

    headerRow: {
        display: "flex",
        gap: 14,
        alignItems: "center",
        paddingTop: 6,
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

    kvGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
        gap: 10,
        marginBottom: 10,
    },

    kvItem: {
        padding: 10,
        borderRadius: 12,
        background: "rgba(255,255,255,0.55)",
        border: "1px solid rgba(127,29,29,0.10)",
    },

    kvLabel: {
        fontSize: 12,
        fontWeight: 800,
        color: "rgba(127,29,29,0.78)",
        marginBottom: 6,
        letterSpacing: -0.1,
    },

    kvValue: {
        fontSize: 13,
        fontWeight: 800,
        color: "#7f1d1d",
        wordBreak: "break-word",
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
};