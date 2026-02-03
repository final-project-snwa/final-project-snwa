import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import Header from '../components/Header';
import { useAuth } from '../contexts/AuthContext';
import { Coins } from 'lucide-react';

const COIN_PACKAGES = [
  { coins: 10, price: 1100, orderName: '코인 10개' },
  { coins: 30, price: 3100, orderName: '코인 30개' },
  { coins: 50, price: 5000, orderName: '코인 50개' },
  { coins: 100, price: 9900, orderName: '코인 100개' },
] as const;

export default function CoinPurchasePage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  const handlePurchase = (pkg: (typeof COIN_PACKAGES)[number]) => {
    alert(`${pkg.orderName} (${pkg.price.toLocaleString()}원) 구매 기능은 준비 중입니다.`);
  };

  if (!user) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-5xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">코인 구매</h1>
        <p className="text-gray-600 mb-8">원하는 코인 패키지를 선택하세요.</p>

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
      </main>
    </div>
  );
}
