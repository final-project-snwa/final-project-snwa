import { useState, useEffect } from 'react';
import { LevelBadge } from '../components/LevelBadge';
import Header from '../components/Header';

interface LeaderboardEntry {
  rank: number;
  userId: number;
  nickname: string | null;
  level: number;
  totalExp: number;
}

export default function LeaderboardPage() {
  const [entries, setEntries] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/exp/leaderboard?limit=20')
      .then((r) => r.json())
      .then((data) => {
        setEntries(Array.isArray(data) ? data : []);
      })
      .catch(() => setEntries([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-2xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">랭킹</h1>

        <div className="mb-6 p-4 bg-white rounded-lg border border-gray-200">
          <h2 className="text-sm font-semibold text-gray-800 mb-3">경험치(EXP) 얻는 방법</h2>
          <ul className="space-y-2 text-sm text-gray-600">
            <li className="flex justify-between">
              <span>출석 (매일 첫 로그인)</span>
              <span className="font-medium text-gray-800">20 + 연속 출석 보너스 EXP</span>
            </li>
            <li className="flex justify-between">
              <span>댓글 작성</span>
              <span className="font-medium text-gray-800">10 EXP (일 20회 한도)</span>
            </li>
            <li className="flex justify-between">
              <span>코인 사용 (기사 번역 보기 등)</span>
              <span className="font-medium text-gray-800">1코인당 20 EXP</span>
            </li>
          </ul>
        </div>

        {loading ? (
          <div className="text-center py-12 text-gray-500">로딩 중...</div>
        ) : entries.length === 0 ? (
          <div className="text-center py-12 text-gray-500">아직 랭킹 데이터가 없습니다.</div>
        ) : (
          <ul className="space-y-0 divide-y divide-gray-200 bg-white rounded-lg border border-gray-200 overflow-hidden">
            {entries.map((e) => (
              <li
                key={e.userId}
                className="flex items-center gap-4 px-4 py-3 hover:bg-gray-50 transition-colors"
              >
                <span
                  className={`w-10 text-center font-bold ${
                    e.rank <= 3 ? 'text-amber-500' : 'text-gray-600'
                  }`}
                >
                  {e.rank}위
                </span>
                <LevelBadge level={e.level} />
                <span className="flex-1 text-gray-900">
                  {e.nickname || `유저${e.userId}`}
                </span>
                <span className="text-sm text-gray-500">
                  {e.totalExp.toLocaleString()} EXP
                </span>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
