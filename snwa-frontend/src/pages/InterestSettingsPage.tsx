import { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
import { Search, Tag, Check, X, User, Trophy, Users, Award } from 'lucide-react';

type InterestType = 'PLAYER' | 'SPORT' | 'TEAM' | 'LEAGUE' | 'OTHER';

interface InterestTarget {
    id: number;
    type: InterestType;
    name: string;
    tagKey: string;
}

interface Subscription {
    id: number;
    target: { id: number; type: InterestType; name: string; tagKey: string };
    isAlarmOn: boolean;
}

/** 타입별 라벨 (OTHER 제외 - #기타 미표시) */
const TYPE_CONFIG: Record<Exclude<InterestType, 'OTHER'>, { label: string; icon: React.ReactNode; color: string }> = {
    PLAYER: { label: '선수', icon: <User className="w-4 h-4" />, color: 'bg-blue-100 text-blue-700 border-blue-200' },
    SPORT: { label: '종목', icon: <Trophy className="w-4 h-4" />, color: 'bg-green-100 text-green-700 border-green-200' },
    TEAM: { label: '팀', icon: <Users className="w-4 h-4" />, color: 'bg-purple-100 text-purple-700 border-purple-200' },
    LEAGUE: { label: '리그', icon: <Award className="w-4 h-4" />, color: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
};

function getAuthHeader(): Record<string, string> | null {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

export default function InterestSettingsPage() {
    const { user } = useAuth();
    const navigate = useNavigate();

    // 검색 상태
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<InterestTarget[]>([]);
    const [searching, setSearching] = useState(false);

    // 구독 상태
    const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
    const [loadingSubscriptions, setLoadingSubscriptions] = useState(true);
    const [togglingId, setTogglingId] = useState<number | null>(null);

    // 전체 태그 상태
    const [allTargets, setAllTargets] = useState<InterestTarget[]>([]);
    const [loadingAllTargets, setLoadingAllTargets] = useState(true);

    // URL 쿼리 파라미터 (기사 해시태그 클릭으로 이동 시)
    const [searchParams] = useSearchParams();

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }
        fetchSubscriptions();
        fetchAllTargets();

        // URL 쿼리 파라미터로 검색어 자동 설정
        const q = searchParams.get('q');
        if (q) {
            setSearchQuery(q);
        }
    }, [user, navigate, searchParams]);

    const fetchSubscriptions = async () => {
        const auth = getAuthHeader();
        if (!auth) return;

        try {
            const res = await fetch('/api/subscriptions/me', { headers: auth });
            if (res.ok) {
                const data: Subscription[] = await res.json();
                setSubscriptions(data);
            }
        } catch (error) {
            console.error('구독 목록 조회 실패:', error);
        } finally {
            setLoadingSubscriptions(false);
        }
    };

    const searchTags = useCallback(async (query: string) => {
        if (!query.trim()) {
            setSearchResults([]);
            return;
        }

        const auth = getAuthHeader();
        if (!auth) return;

        setSearching(true);
        try {
            const res = await fetch(`/api/targets?keyword=${encodeURIComponent(query)}`, {
                headers: auth
            });
            if (res.ok) {
                const data: InterestTarget[] = await res.json();
                setSearchResults(data);
            }
        } catch (error) {
            console.error('태그 검색 실패:', error);
        } finally {
            setSearching(false);
        }
    }, []);

    const fetchAllTargets = async () => {
        const auth = getAuthHeader();
        if (!auth) return;

        setLoadingAllTargets(true);
        try {
            const res = await fetch('/api/targets/all', { headers: auth });
            if (res.ok) {
                const data: InterestTarget[] = await res.json();
                setAllTargets(data);
            }
        } catch (error) {
            console.error('전체 태그 조회 실패:', error);
        } finally {
            setLoadingAllTargets(false);
        }
    };

    // 디바운스 검색
    useEffect(() => {
        const timer = setTimeout(() => {
            searchTags(searchQuery);
        }, 300);
        return () => clearTimeout(timer);
    }, [searchQuery, searchTags]);

    const toggleSubscription = async (targetId: number) => {
        const auth = getAuthHeader();
        if (!auth) return;

        setTogglingId(targetId);
        try {
            const res = await fetch(`/api/subscriptions/${targetId}`, {
                method: 'POST',
                headers: auth,
            });

            if (res.ok) {
                // 구독 목록 새로고침
                await fetchSubscriptions();
            }
        } catch (error) {
            console.error('구독 토글 실패:', error);
        } finally {
            setTogglingId(null);
        }
    };

    const isSubscribed = (targetId: number): boolean => {
        return subscriptions.some(sub => sub.target.id === targetId);
    };

    /** 구독 취소만 수행 (이미 구독 중일 때만 호출) */
    const unsubscribe = async (targetId: number) => {
        if (!isSubscribed(targetId)) return;
        await toggleSubscription(targetId);
    };

    // 구독 목록을 타입별로 그룹화 (#기타 섹션 없음: OTHER는 별도 플랫 리스트로)
    const groupedSubscriptions = subscriptions.reduce((acc, sub) => {
        const type = sub.target?.type ?? 'OTHER';
        if (!acc[type]) acc[type] = [];
        acc[type].push(sub);
        return acc;
    }, {} as Record<InterestType, Subscription[]>);

    const displayTypes: InterestType[] = ['PLAYER', 'SPORT', 'TEAM', 'LEAGUE'];

    // 전체 태그를 타입별로 그룹화
    const groupedAllTargets = allTargets.reduce((acc, target) => {
        const type = target.type ?? 'OTHER';
        if (!acc[type]) acc[type] = [];
        acc[type].push(target);
        return acc;
    }, {} as Record<InterestType, InterestTarget[]>);

    if (!user) {
        return null;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Header />

            <main className="max-w-6xl mx-auto px-4 py-8">
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">관심사 설정</h1>
                        <p className="text-gray-500 mt-1">관심 있는 태그를 구독하면 새 기사 알림을 받을 수 있습니다</p>
                    </div>
                    <Link
                        to="/profile"
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        프로필 설정
                    </Link>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* 검색 및 결과 섹션 */}
                    <div className="lg:col-span-2 space-y-6">
                        {/* 검색창 */}
                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                            <div className="text-center mb-4">
                                <h2 className="text-xl font-bold text-gray-900 mb-1">관심사 검색</h2>
                                <p className="text-gray-500 text-xs">선수, 팀, 리그 등 관심 있는 키워드를 검색해보세요</p>
                            </div>

                            <div className="relative flex items-center">
                                {!searchQuery && <Search className="absolute left-6 w-4 h-4 text-gray-400 pointer-events-none" />}
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="손흥민, 토트넘, 프리미어리그..."
                                    className="w-full py-2.5 rounded-lg border border-gray-300 bg-gray-50 focus:border-gray-900 focus:bg-white focus:outline-none transition-all text-sm placeholder:text-gray-400 text-left"
                                    style={{ paddingLeft: '46px', paddingRight: '46px' }}
                                />
                                {searching ? (
                                    <div className="absolute right-4 top-1/2 -translate-y-1/2">
                                        <div className="w-4 h-4 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
                                    </div>
                                ) : searchQuery && (
                                    <button
                                        onClick={() => setSearchQuery('')}
                                        className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center transition-colors"
                                    >
                                        <X className="w-3 h-3 text-gray-500" />
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* 검색 결과 */}
                        {searchQuery && (
                            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                                <h2 className="text-lg font-bold text-gray-900 mb-4">
                                    검색 결과 {searchResults.length > 0 && `(${searchResults.length}개)`}
                                </h2>

                                {searchResults.length === 0 ? (
                                    <div className="text-center py-8">
                                        <Tag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                                        <p className="text-gray-500">
                                            {searching ? '검색 중...' : '검색 결과가 없습니다'}
                                        </p>
                                    </div>
                                ) : (
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        {searchResults.map((target) => {
                                            const subscribed = isSubscribed(target.id);
                                            const toggling = togglingId === target.id;

                                            return (
                                                <div
                                                    key={target.id}
                                                    className={`flex items-center justify-between p-4 rounded-lg border-2 transition-all ${subscribed
                                                        ? 'border-gray-900 bg-gray-50'
                                                        : 'border-gray-200 hover:border-gray-300'
                                                        }`}
                                                >
                                                    <span className="font-medium text-gray-900">{target.name}</span>

                                                    <button
                                                        onClick={() => toggleSubscription(target.id)}
                                                        disabled={toggling}
                                                        className={`flex items-center gap-1 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${subscribed
                                                            ? 'bg-gray-900 text-white hover:bg-gray-800'
                                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                                            }`}
                                                    >
                                                        {toggling ? (
                                                            <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
                                                        ) : subscribed ? (
                                                            <>
                                                                <Check className="w-4 h-4" />
                                                                구독중
                                                            </>
                                                        ) : (
                                                            '구독'
                                                        )}
                                                    </button>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
                        )}

                        {/* 전체 태그 목록 */}
                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                            <h2 className="text-lg font-bold text-gray-900 mb-4">
                                전체 태그 목록
                                {allTargets.length > 0 && (
                                    <span className="ml-2 text-sm font-normal text-gray-500">
                                        ({allTargets.length}개)
                                    </span>
                                )}
                            </h2>

                            {loadingAllTargets ? (
                                <div className="space-y-3">
                                    {[1, 2, 3].map((i) => (
                                        <div key={i} className="h-10 bg-gray-100 rounded-lg animate-pulse"></div>
                                    ))}
                                </div>
                            ) : allTargets.length === 0 ? (
                                <div className="text-center py-8">
                                    <Tag className="w-10 h-10 text-gray-300 mx-auto mb-2" />
                                    <p className="text-sm text-gray-500">등록된 태그가 없습니다</p>
                                </div>
                            ) : (
                                <div className="space-y-6">
                                    {displayTypes.map((type) => {
                                        const targets = groupedAllTargets[type];
                                        if (!targets || targets.length === 0) return null;
                                        const config = TYPE_CONFIG[type as Exclude<InterestType, 'OTHER'>];
                                        return (
                                            <div key={type}>
                                                <div className="flex items-center gap-2 mb-3">
                                                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border ${config.color}`}>
                                                        {config.icon}
                                                        {config.label}
                                                    </span>
                                                    <span className="text-xs text-gray-400">{targets.length}개</span>
                                                </div>
                                                <div className="flex flex-wrap gap-2">
                                                    {targets.map((target) => {
                                                        const subscribed = isSubscribed(target.id);
                                                        const toggling = togglingId === target.id;
                                                        return (
                                                            <button
                                                                key={target.id}
                                                                onClick={() => toggleSubscription(target.id)}
                                                                disabled={toggling}
                                                                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium border transition-all ${subscribed
                                                                        ? 'bg-gray-900 text-white border-gray-900 hover:bg-gray-800'
                                                                        : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50 hover:border-gray-300'
                                                                    }`}
                                                            >
                                                                {toggling ? (
                                                                    <div className="w-3.5 h-3.5 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
                                                                ) : subscribed ? (
                                                                    <Check className="w-3.5 h-3.5" />
                                                                ) : null}
                                                                {target.name}
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            </div>
                                        );
                                    })}

                                    {/* 기타 타입 */}
                                    {(groupedAllTargets.OTHER?.length ?? 0) > 0 && (
                                        <div>
                                            <div className="flex items-center gap-2 mb-3">
                                                <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border bg-gray-100 text-gray-600 border-gray-200">
                                                    <Tag className="w-3.5 h-3.5" />
                                                    기타
                                                </span>
                                                <span className="text-xs text-gray-400">{groupedAllTargets.OTHER!.length}개</span>
                                            </div>
                                            <div className="flex flex-wrap gap-2">
                                                {groupedAllTargets.OTHER!.map((target) => {
                                                    const subscribed = isSubscribed(target.id);
                                                    const toggling = togglingId === target.id;
                                                    return (
                                                        <button
                                                            key={target.id}
                                                            onClick={() => toggleSubscription(target.id)}
                                                            disabled={toggling}
                                                            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium border transition-all ${subscribed
                                                                    ? 'bg-gray-900 text-white border-gray-900 hover:bg-gray-800'
                                                                    : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50 hover:border-gray-300'
                                                                }`}
                                                        >
                                                            {toggling ? (
                                                                <div className="w-3.5 h-3.5 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
                                                            ) : subscribed ? (
                                                                <Check className="w-3.5 h-3.5" />
                                                            ) : null}
                                                            {target.name}
                                                        </button>
                                                    );
                                                })}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>


                    </div>

                    {/* 내 구독 목록 사이드바 */}
                    <div className="lg:col-span-1">
                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 sticky top-24">
                            <h2 className="text-lg font-bold text-gray-900 mb-4">
                                내 구독 목록
                                {subscriptions.length > 0 && (
                                    <span className="ml-2 text-sm font-normal text-gray-500">
                                        ({subscriptions.length}개)
                                    </span>
                                )}
                            </h2>

                            {loadingSubscriptions ? (
                                <div className="space-y-3">
                                    {[1, 2, 3].map((i) => (
                                        <div key={i} className="h-10 bg-gray-100 rounded-lg animate-pulse"></div>
                                    ))}
                                </div>
                            ) : subscriptions.length === 0 ? (
                                <div className="text-center py-8">
                                    <Tag className="w-10 h-10 text-gray-300 mx-auto mb-2" />
                                    <p className="text-sm text-gray-500">
                                        아직 구독한 태그가 없습니다
                                    </p>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {/* 선수/종목/팀/리그만 타입 라벨로 표시 (#기타 없음) */}
                                    {displayTypes.map((type) => {
                                        const subs = groupedSubscriptions[type];
                                        if (!subs || subs.length === 0) return null;
                                        const config = TYPE_CONFIG[type as Exclude<InterestType, 'OTHER'>];
                                        return (
                                            <div key={type}>
                                                <div className="flex items-center gap-2 mb-2">
                                                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border ${config.color}`}>
                                                        {config.icon}
                                                        {config.label}
                                                    </span>
                                                    <span className="text-xs text-gray-400">{subs.length}</span>
                                                </div>
                                                <div className="flex flex-wrap gap-2">
                                                    {subs.map((sub) => (
                                                        <span
                                                            key={sub.id}
                                                            className="group inline-flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 rounded-full text-sm text-gray-700"
                                                        >
                                                            <span>{sub.target.name}</span>
                                                            <button
                                                                type="button"
                                                                onClick={() => unsubscribe(sub.target.id)}
                                                                disabled={togglingId === sub.target.id}
                                                                className="inline-flex items-center gap-0.5 text-red-500 hover:text-red-600 hover:bg-red-50 rounded p-0.5 transition-colors"
                                                                title="구독 취소"
                                                            >
                                                                <X className="w-3.5 h-3.5" />
                                                                <span className="text-xs">구독 취소</span>
                                                            </button>
                                                        </span>
                                                    ))}
                                                </div>
                                            </div>
                                        );
                                    })}
                                    {/* 기타 타입은 라벨 없이 태그만 나열 */}
                                    {(groupedSubscriptions.OTHER?.length ?? 0) > 0 && (
                                        <div>
                                            <div className="flex flex-wrap gap-2">
                                                {groupedSubscriptions.OTHER!.map((sub) => (
                                                    <span
                                                        key={sub.id}
                                                        className="group inline-flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 rounded-full text-sm text-gray-700"
                                                    >
                                                        <span>{sub.target.name}</span>
                                                        <button
                                                            type="button"
                                                            onClick={() => unsubscribe(sub.target.id)}
                                                            disabled={togglingId === sub.target.id}
                                                            className="inline-flex items-center gap-0.5 text-red-500 hover:text-red-600 hover:bg-red-50 rounded p-0.5 transition-colors"
                                                            title="구독 취소"
                                                        >
                                                            <X className="w-3.5 h-3.5" />
                                                            <span className="text-xs">구독 취소</span>
                                                        </button>
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
