import { useEffect, useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';

type CrawlingStatus = 'SUCCESS' | 'FAILURE' | 'RUNNING' | 'PAUSED';

type CrawlingLog = {
    logId: number;
    jobId: number;
    jobName: string;
    status: CrawlingStatus;
    collectedCount: number;
    message: string;
    startTime: string;
    endTime: string;
    durationSeconds: number;
};

// 로그 페이징 응답 구조 (Spring Page 객체 대응)
type PageResponse<T> = {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // currentPage (0-indexed)
};

function getAuthHeader() {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

export default function CrawlerLogs() {
    const [logs, setLogs] = useState<CrawlingLog[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchLogs = async (pageIndex: number) => {
        const auth = getAuthHeader();
        if (!auth) return;

        setLoading(true);
        setError('');
        try {
            // size=20, sort=startTime,DESC 기본
            const res = await fetch(`/api/admin/crawler/logs?page=${pageIndex}&size=20`, { headers: { ...auth } });
            if (res.ok) {
                const data: PageResponse<CrawlingLog> = await res.json();
                setLogs(data.content);
                setTotalPages(data.totalPages);
                setPage(data.number); // 서버가 돌려준 페이지 번호 사용
            } else {
                setError('로그를 불러오는데 실패했습니다.');
            }
        } catch (e) {
            setError('로그 로딩 중 오류 발생');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs(0);
    }, []);

    const getStatusBadge = (status: CrawlingStatus) => {
        switch (status) {
            case 'SUCCESS':
                return <Badge className="bg-green-500 hover:bg-green-600">성공</Badge>;
            case 'FAILURE':
                return <Badge variant="destructive">실패</Badge>;
            case 'RUNNING':
                return <Badge className="bg-blue-500 hover:bg-blue-600 animate-pulse">실행중</Badge>;
            case 'PAUSED':
                return <Badge variant="secondary">중지됨</Badge>;
            default:
                return <Badge variant="outline">{status}</Badge>;
        }
    };

    return (
        <div className="space-y-4">
            <div className="flex justify-between items-center">
                <h3 className="text-lg font-medium">실행 이력</h3>
                <button 
                    onClick={() => fetchLogs(page)} 
                    className="text-sm text-gray-500 hover:text-gray-900 flex items-center gap-1"
                >
                    새로고침
                </button>
            </div>

            {error && <div className="text-red-500 text-sm">{error}</div>}

            <div className="rounded-md border bg-white">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[80px]">ID</TableHead>
                            <TableHead>작업명</TableHead>
                            <TableHead>상태</TableHead>
                            <TableHead>결과</TableHead>
                            <TableHead>수집량</TableHead>
                            <TableHead className="text-right">소요시간</TableHead>
                            <TableHead className="text-right">시작시간</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {loading && logs.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} className="text-center py-8">로딩 중...</TableCell>
                            </TableRow>
                        ) : logs.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} className="text-center py-8">로그가 없습니다.</TableCell>
                            </TableRow>
                        ) : (
                            logs.map((log) => (
                                <TableRow key={log.logId}>
                                    <TableCell className="font-medium">{log.logId}</TableCell>
                                    <TableCell>{log.jobName}</TableCell>
                                    <TableCell>{getStatusBadge(log.status)}</TableCell>
                                    <TableCell className="max-w-[300px] truncate" title={log.message}>
                                        {log.message || '-'}
                                    </TableCell>
                                    <TableCell>{log.collectedCount}건</TableCell>
                                    <TableCell className="text-right">{log.durationSeconds}초</TableCell>
                                    <TableCell className="text-right">
                                        {new Date(log.startTime).toLocaleString('ko-KR')}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-center space-x-2 py-4">
                <button
                    className="px-3 py-1 border rounded text-sm disabled:opacity-50"
                    onClick={() => fetchLogs(page - 1)}
                    disabled={page === 0 || loading}
                >
                    이전
                </button>
                <span className="text-sm text-gray-600">
                    {page + 1} / {totalPages === 0 ? 1 : totalPages}
                </span>
                <button
                    className="px-3 py-1 border rounded text-sm disabled:opacity-50"
                    onClick={() => fetchLogs(page + 1)}
                    disabled={page >= totalPages - 1 || loading}
                >
                    다음
                </button>
            </div>
        </div>
    );
}
