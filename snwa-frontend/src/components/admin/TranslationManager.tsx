import { useEffect, useState, useMemo } from 'react';
import { Search, Eye, Languages, Tag } from 'lucide-react';

type AdminTranslation = {
    id: number;
    translatedTitle: string;
    translatedContent: string;
    summary: string;
    tagNames: string[];
};

export default function TranslationManager() {
    const [data, setData] = useState<AdminTranslation[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');
    const [selected, setSelected] = useState<AdminTranslation | null>(null);

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
        const list = !search.trim()
            ? data
            : data.filter(t => {
                const q = search.trim().toLowerCase();
                return (
                    t.translatedTitle.toLowerCase().includes(q) ||
                    String(t.id).includes(search) ||
                    t.tagNames.some(tag => tag.toLowerCase().includes(q))
                );
            });
        return [...list].sort((a, b) => a.id - b.id);
    }, [data, search]);

    if (loading) return <div className="py-10 text-center text-gray-500">데이터 분석 정보를 불러오는 중...</div>;
    if (error) return <div className="py-10 text-center text-red-500">{error}</div>;

    return (
        <div className="space-y-6">
            {/* 상단 검색바 */}
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
                <div className="text-sm text-gray-500">총 {filtered.length}건 분석됨</div>
            </div>

            {/* 테이블 영역 */}
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="min-w-[900px] w-full text-sm">
                        <thead className="bg-gray-50 text-gray-600 border-b border-gray-200">
                        <tr>
                            <th className="text-left px-6 py-3 font-medium w-20">ID</th>
                            <th className="text-left px-6 py-3 font-medium">번역된 제목</th>
                            <th className="text-left px-6 py-3 font-medium w-1/3">요약 미리보기</th>
                            <th className="text-left px-6 py-3 font-medium">태그</th>
                            <th className="text-center px-6 py-3 font-medium w-20">본문</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {filtered.map((t) => (
                            <tr key={t.id} className="hover:bg-gray-50 text-gray-900">
                                <td className="px-6 py-4 text-gray-500">#{t.id}</td>
                                <td className="px-6 py-4 font-medium">{t.translatedTitle}</td>
                                <td className="px-6 py-4 text-gray-600 line-clamp-2 mt-4">{t.summary}</td>
                                <td className="px-6 py-4">
                                    <div className="flex flex-wrap gap-1">
                                        {t.tagNames.map((name, i) => (
                                            <span key={i} className="bg-sky-50 text-sky-600 px-2 py-0.5 rounded text-[10px] border border-sky-100 font-medium">
                          #{name}
                        </span>
                                        ))}
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-center">
                                    <button onClick={() => setSelected(t)} className="text-gray-400 hover:text-sky-600 transition-colors">
                                        <Eye className="w-5 h-5" />
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* 상세보기 모달 (포탈 없이 조건부 렌더링) */}
            {selected && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={() => setSelected(null)}>
                    <div className="bg-white rounded-xl shadow-2xl max-w-3xl w-full max-h-[80vh] flex flex-col" onClick={e => e.stopPropagation()}>
                        <div className="p-4 border-b border-gray-100 flex justify-between items-center">
                            <div className="flex items-center gap-2 text-sky-600">
                                <Languages className="w-5 h-5" />
                                <h3 className="font-bold">번역 및 요약 분석 상세 (ID: {selected.id})</h3>
                            </div>
                            <button onClick={() => setSelected(null)} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
                        </div>
                        <div className="p-6 overflow-y-auto space-y-6">
                            <section>
                                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 flex items-center gap-1">
                                    <Tag className="w-3 h-3" /> AI Summary
                                </h4>
                                <div className="bg-gray-50 p-4 rounded-lg text-sm text-gray-700 leading-relaxed border border-gray-100">
                                    {selected.summary}
                                </div>
                            </section>
                            <section>
                                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 flex items-center gap-1">
                                    Translated Content
                                </h4>
                                <div className="text-sm text-gray-800 leading-8 whitespace-pre-wrap">
                                    {selected.translatedContent}
                                </div>
                            </section>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}