import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import Header from '../components/Header';
import { useAuth } from '../contexts/AuthContext';
import { Coins } from 'lucide-react';

const COIN_PACKAGES = [
  { policyId: 1, coins: 10, price: 1100, orderName: '코인 10개' },
  { policyId: 2, coins: 30, price: 3100, orderName: '코인 30개' },
  { policyId: 3, coins: 50, price: 5000, orderName: '코인 50개' },
  { policyId: 4, coins: 100, price: 9900, orderName: '코인 100개' },
] as const;

export default function CoinPurchasePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [balance, setBalance] = useState<number | null>(null);
  const [coinHistory, setCoinHistory] = useState<any[]>([]);
  const [coinLoading, setCoinLoading] = useState(false);
  const [coinError, setCoinError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  useEffect(() => {
    if (!user) return;
    const token = sessionStorage.getItem('snwa_token');
    if (!token) {
      setCoinError('로그인이 필요합니다.');
      return;
    }

    let cancelled = false;
    setCoinLoading(true);
    setCoinError(null);

    Promise.all([
      fetch('/api/coins/me', {
        headers: { Authorization: `Bearer ${token}` },
      }),
      fetch('/api/coins/history', {
        headers: { Authorization: `Bearer ${token}` },
      }),
    ])
      .then(async ([balanceRes, historyRes]) => {
        if (!balanceRes.ok) throw new Error(`잔액 조회 실패: ${balanceRes.status}`);
        if (!historyRes.ok) throw new Error(`내역 조회 실패: ${historyRes.status}`);

        const balanceBody = await balanceRes.json();
        const historyBody = await historyRes.json();

        if (cancelled) return;
        setBalance(balanceBody?.balance ?? 0);
        setCoinHistory(Array.isArray(historyBody) ? historyBody : []);
      })
      .catch((e) => {
        if (cancelled) return;
        setCoinError(e?.message ?? '코인 정보를 불러오지 못했습니다.');
      })
      .finally(() => {
        if (cancelled) return;
        setCoinLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [user]);

  const handlePurchase = (pkg: (typeof COIN_PACKAGES)[number]) => {
    navigate('/pay', { state: { policyId: pkg.policyId } });
  };

  if (!user) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-5xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">코인 구매</h1>
        <p className="text-gray-600 mb-8">원하는 코인 패키지를 선택하세요.</p>

        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">내 코인</h2>
          {coinLoading && <p className="text-sm text-gray-500">불러오는 중...</p>}
          {coinError && <p className="text-sm text-red-600">{coinError}</p>}
          {!coinLoading && !coinError && (
            <>
              <p className="text-2xl font-bold text-gray-900">
                {balance?.toLocaleString() ?? 0} 코인
              </p>
              <p className="text-xs text-gray-500 mt-1">최근 거래 기준 잔액</p>
            </>
          )}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {COIN_PACKAGES.map((pkg) => (
            <div
              key={pkg.coins}
              className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden hover:shadow-md transition-shadow flex flex-col"
            >
              {/* 이미지 영역 - 코인 비주얼 */}
              <div className="aspect-[4/3] bg-gradient-to-br from-amber-100 to-amber-200 flex items-center justify-center p-4">
                <div className="flex flex-col items-center gap-1">
                  <Coins className="w-14 h-14 text-amber-600" strokeWidth={1.5} />
                  <span style={{ letterSpacing: '0.1em' }} className="text-lg font-bold text-amber-800">{pkg.coins}코인</span>
                </div>
              </div>

              <div className="p-4 flex-1 flex flex-col">
                <h3 style={{ letterSpacing: '0.08em' }} className="font-semibold text-gray-900 mb-1">{pkg.orderName}</h3>

                <div className="mt-auto flex items-end justify-between gap-2">
                  <span style={{ letterSpacing: '0.05em' }} className="text-lg font-bold text-gray-900">
                    {pkg.price.toLocaleString()}원
                  </span>
                  <button
                    type="button"
                    onClick={() => handlePurchase(pkg)}
                    style={{ letterSpacing: '0.1em' }}
                    className="px-4 py-2 text-sm font-medium rounded-lg bg-gray-900 text-white hover:bg-gray-800 transition-colors"
                  >
                    구매하기
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mt-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">코인 내역</h2>
          {coinLoading && <p className="text-sm text-gray-500">불러오는 중...</p>}
          {!coinLoading && coinHistory.length === 0 && (
            <p className="text-sm text-gray-500">코인 내역이 없습니다.</p>
          )}
          {!coinLoading && coinHistory.length > 0 && (
            <div className="space-y-3">
              {coinHistory.map((tx) => {
                const created = tx.createdAt ?? tx.createdDate;
                const normalizedCreated = created && /[zZ]|[+-]\d{2}:\d{2}$/.test(created)
                  ? created
                  : (created ? `${created}Z` : null);
                const createdLabel = normalizedCreated
                  ? new Date(normalizedCreated).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })
                  : '-';
                const typeLabel =
                  tx.type === 'SPEND'
                    ? '사용'
                    : tx.type === 'CHARGE'
                      ? '충전'
                      : tx.type === 'ATTENDANCE_REWARD'
                        ? '출석 보상'
                        : tx.type === 'REFUND'
                          ? '회수'
                          : tx.type;
                const isMinus = tx.type === 'SPEND' || tx.type === 'REFUND';
                return (
                  <div
                    key={tx.id}
                    className="flex flex-col gap-2 rounded-lg border border-gray-200 p-4 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-900">{typeLabel}</p>
                      <p className="text-xs text-gray-500">{createdLabel}</p>
                    </div>
                    <div className="text-right">
                      <p className={`text-sm font-semibold ${isMinus ? 'text-red-600' : 'text-green-600'}`}>
                        {isMinus ? '-' : '+'}
                        {tx.amount}
                      </p>
                      <p className="text-xs text-gray-500">잔액 {tx.balanceAfter}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
