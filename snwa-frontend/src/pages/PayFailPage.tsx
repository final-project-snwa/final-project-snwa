import { useSearchParams } from "react-router";

export default function PayFailPage() {
    const [sp] = useSearchParams();
    return (
        <div style={{ padding: 24 }}>
            <h1>결제 실패</h1>
            <pre>{sp.toString()}</pre>
        </div>
    );
}
