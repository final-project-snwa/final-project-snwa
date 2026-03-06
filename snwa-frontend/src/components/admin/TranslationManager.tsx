import { useEffect, useState, useMemo } from 'react';
import { Search, Eye, Tag, Globe } from 'lucide-react';

// [변경] 백엔드 DTO 구조에 맞춘 새 타입 정의
type TranslationDetail = {
    language: string;          // KO, JA, ZH
    translatedTitle: string;
    translatedContent: string;
    summary: string;
    tagNames: string[];
};

type AdminArticleGroup = {
    id: number;
    originalTitle: string;    // 원본 제목 (번역 전용)
    translations: TranslationDetail[];
};

export default function TranslationManager() {
    const [data, setData] = useState<AdminArticleGroup[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    // 상세보기 모달용 상태
    const [selectedDetail, setSelectedDetail] = useState<{ id: number; detail: TranslationDetail } | null>(null);

    const fetchTranslations = async () => {
        const token = sessionStorage.getItem('snwa_token');
        try {
            setLoading(true);
            const res = await fetch('/api/admin/articles/translations', {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error('데이터 로드 실패');
            const json = await res.json();
            setData(json);
        } catch (e) {
            setError(e instanceof Error ? e.message : '데이터를 불러오지 못했습니다.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTranslations();
    }, []);

    const filtered = useMemo(() => {
        if (!search.trim()) return data;
        const q = search.trim().toLowerCase();
        return data.filter(group => {
            // 검색: ID나 번역본 중 하나라도 검색어 포함되면 표시
            if (String(group.id).includes(q)) return true;
            return group.translations.some(t => {
                const titleMatch = (t.translatedTitle || '').toLowerCase().includes(q);
                const tagMatch = (t.tagNames || []).some(tag => (tag || '').toLowerCase().includes(q));
                return titleMatch || tagMatch;
            });
        });
    }, [data, search]);

    if (loading) return <div className="py-10 text-center text-gray-500">데이터를 불러오는 중...</div>;
    if (error) return <div className="py-10 text-center text-red-500">{error}</div>;

    return (
        <div className="space-y-6">
            {/* 검색바 */}
            <div className="flex items-center gap-4">
                <div className="relative flex-1 max-w-sm">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                        type="text"
                        placeholder="ID, 제목, 태그로 검색..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                </div>
                <div className="text-sm text-gray-500">총 {filtered.length}건 (기사 기준)</div>
            </div>

            {/* 테이블 */}
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="min-w-[900px] w-full text-sm">
                        <thead className="bg-gray-50 text-gray-600 border-b border-gray-200">
                            <tr>
                                <th className="text-left px-6 py-3 font-medium w-20">ID</th>
                                <th className="text-left px-6 py-3 font-medium w-24">언어</th>
                                <th className="text-left px-6 py-3 font-medium">번역된 제목</th>
                                <th className="text-left px-6 py-3 font-medium w-1/3">요약 미리보기</th>
                                <th className="text-left px-6 py-3 font-medium w-40">태그</th>
                                <th className="text-center px-6 py-3 font-medium w-20">본문</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                            {filtered.map((group) => (
                                <TranslationRow
                                    key={group.id}
                                    group={group}
                                    onDetailView={(detail) => setSelectedDetail({ id: group.id, detail })}
                                />
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* 본문 상세보기 모달 */}
            {selectedDetail && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={() => setSelectedDetail(null)}>
                    <div className="bg-white rounded-xl shadow-2xl max-w-3xl w-full max-h-[80vh] flex flex-col" onClick={e => e.stopPropagation()}>
                        <div className="p-4 border-b border-gray-100 flex justify-between items-center">
                            <div className="flex items-center gap-2 text-sky-600">
                                <Globe className="w-5 h-5" />
                                <h3 className="font-bold">
                                    기사 #{selectedDetail.id} - [{selectedDetail.detail.language}] 상세
                                </h3>
                            </div>
                            <button onClick={() => setSelectedDetail(null)} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
                        </div>
                        <div className="p-6 overflow-y-auto space-y-6">
                            <section>
                                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 flex items-center gap-1">
                                    <Tag className="w-3 h-3" /> AI Summary
                                </h4>
                                <div className="bg-gray-50 p-4 rounded-lg text-sm text-gray-700 leading-relaxed border border-gray-100">
                                    {selectedDetail.detail.summary}
                                </div>
                            </section>
                            <section>
                                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 flex items-center gap-1">
                                    Translated Content
                                </h4>
                                <div className="text-sm text-gray-800 leading-8 whitespace-pre-wrap">
                                    {selectedDetail.detail.translatedContent}
                                </div>
                            </section>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

// [내부 컴포넌트] 기사 1개를 표시하는 행 (언어 탭 상태 관리)
function TranslationRow({ group, onDetailView }: { group: AdminArticleGroup, onDetailView: (d: TranslationDetail) => void }) {
    // 기본적으로 첫 번째 번역본(보통 KO)을 보여줌
    const [activeLang, setActiveLang] = useState(group.translations[0]?.language || '');

    // 현재 선택된 언어의 데이터 찾기
    const current = group.translations.find(t => t.language === activeLang) || group.translations[0];

    if (!current) return null; // 번역본 없으면 안 보임

    return (
        <tr className="hover:bg-gray-50 text-gray-900 group">
            <td className="px-6 py-4 text-gray-500 align-top">#{group.id}</td>
            <td className="px-6 py-4 align-top">
                {/* 언어 선택 버튼들 */}
                <div className="flex flex-col gap-1">
                    {[...group.translations].sort((a, b) => {
                        if (a.language === 'KO') return -1;
                        if (b.language === 'KO') return 1;
                        return a.language.localeCompare(b.language);
                    }).map(t => (
                        <button
                            key={t.language}
                            onClick={() => setActiveLang(t.language)}
                            className={`text-xs px-2 py-1 rounded font-bold transition-colors border ${activeLang === t.language
                                ? 'bg-sky-100 text-sky-700 border-sky-200'
                                : 'bg-white text-gray-400 border-gray-200 hover:bg-gray-100'
                                }`}
                        >
                            {t.language}
                        </button>
                    ))}
                </div>
            </td>
            <td className="px-6 py-4 font-medium align-top">
                {current.translatedTitle || (
                    <span className="text-gray-400 italic font-normal text-xs">
                        [Original] {group.originalTitle}
                    </span>
                )}
            </td>
            <td className="px-6 py-4 text-gray-600 line-clamp-2 mt-4 align-top text-xs leading-relaxed">
                {current.summary ? (current.summary.length > 100 ? current.summary.substring(0, 100) + '...' : current.summary) : '-'}
            </td>
            <td className="px-6 py-4 align-top">
                <div className="flex flex-wrap gap-1">
                    {current.tagNames && current.tagNames.map((name, i) => (
                        <span key={i} className="bg-sky-50 text-sky-600 px-2 py-0.5 rounded text-[10px] border border-sky-100 font-medium">
                            #{name}
                        </span>
                    ))}
                </div>
            </td>
            <td className="px-6 py-4 text-center align-top">
                <button
                    onClick={() => onDetailView(current)}
                    className="text-gray-400 hover:text-sky-600 transition-colors pt-1"
                >
                    <Eye className="w-5 h-5" />
                </button>
            </td>
        </tr>
    );
}