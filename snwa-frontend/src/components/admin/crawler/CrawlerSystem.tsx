import { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Activity, Cpu, Server, Play, Square } from 'lucide-react';
import { toast } from 'sonner';

type SystemStatus = {
    totalMemory: string;
    freeMemory: string;
    usedMemory: string;
    availableProcessors: number;
    activeSystemThreads: number;
    runningCrawlerCount: number;
    runningJobIds: number[];
};

function getAuthHeader() {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

export default function CrawlerSystem() {
    const [status, setStatus] = useState<SystemStatus | null>(null);
    const [loading, setLoading] = useState(false);
    const [refreshTrigger, setRefreshTrigger] = useState(0);

    useEffect(() => {
        const fetchStatus = async () => {
             const auth = getAuthHeader();
             if (!auth) return;

             try {
                 const res = await fetch('/api/admin/crawler/status', { headers: { ...auth } });
                 if (res.ok) {
                     const data = await res.json();
                     setStatus(data);
                 }
             } catch (error) {
                 console.error("System status fetch failed", error);
             }
        };

        fetchStatus();
        // 5초마다 자동 갱신
        const interval = setInterval(fetchStatus, 5000);
        return () => clearInterval(interval);
    }, [refreshTrigger]);

    const handleGlobalAction = async (action: 'start' | 'stop') => {
        const auth = getAuthHeader();
        if (!auth) return;
        
        const confirmMsg = action === 'start' 
            ? "모든 크롤링 작업을 시작하시겠습니까?" 
            : "긴급 중단: 모든 크롤링 작업을 즉시 중단하시겠습니까?";
        
        if (!confirm(confirmMsg)) return;

        setLoading(true);
        try {
            const res = await fetch(`/api/admin/crawler/global/${action}`, { 
                method: 'POST',
                headers: { ...auth } 
            });
            const text = await res.text();
            
            if (res.ok) {
                toast.success(text);
                setRefreshTrigger(p => p + 1); // 상태 즉시 갱신
            } else {
                toast.error(`작업 실패: ${text}`);
            }
        } catch (e) {
            toast.error("요청 처리 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    if (!status) return <div className="p-8 text-center text-gray-500">시스템 상태를 불러오는 중...</div>;

    // Parse memory strings (e.g., "512MB") to numbers
    const parseMemory = (memStr: string) => parseInt(memStr.replace('MB', '')) || 0;
    const usedMb = parseMemory(status.usedMemory);
    const totalMb = parseMemory(status.totalMemory);
    const memoryUsagePercent = totalMb > 0 ? Math.round((usedMb / totalMb) * 100) : 0;

    return (
        <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Active Threads</CardTitle>
                        <Activity className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{status.activeSystemThreads}</div>
                        <p className="text-xs text-muted-foreground">현재 활성 스레드 / Core: {status.availableProcessors}</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Memory Usage</CardTitle>
                        <Server className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{memoryUsagePercent}%</div>
                        <p className="text-xs text-muted-foreground">
                            {status.usedMemory} / {status.totalMemory}
                        </p>
                        <div className="w-full bg-gray-200 rounded-full h-1.5 mt-2">
                            <div 
                                className={`h-1.5 rounded-full ${memoryUsagePercent > 80 ? 'bg-red-500' : 'bg-blue-500'}`} 
                                style={{ width: `${memoryUsagePercent}%` }}
                            ></div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Running Crawlers</CardTitle>
                        <Cpu className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{status.runningCrawlerCount}</div>
                        <p className="text-xs text-muted-foreground">
                            실행 중인 작업(Job) 수
                        </p>
                    </CardContent>
                </Card>
            </div>

            <Card className="border-black border-2 bg-red-50/10">
                <CardHeader>
                    <CardTitle className="text-lg">Global Controls</CardTitle>
                    <CardDescription>전체 시스템에 영향을 주는 긴급 제어 버튼입니다.</CardDescription>
                </CardHeader>
                <CardContent className="flex gap-4">
                    <Button 
                        variant="destructive" 
                        size="lg" 
                        onClick={() => handleGlobalAction('stop')}
                        disabled={loading}
                        className="w-full md:w-auto bg-red-600 hover:bg-red-700 text-white"
                    >
                        <Square className="mr-2 h-4 w-4 fill-current" />
                        EMERGENCY STOP (전체 중단)
                    </Button>
                    <Button 
                        variant="default" 
                        size="lg" 
                        onClick={() => handleGlobalAction('start')}
                        disabled={loading}
                        className="w-full md:w-auto bg-blue-600 hover:bg-blue-700 text-white"
                    >
                        <Play className="mr-2 h-4 w-4 fill-current" />
                        START ALL (전체 재개)
                    </Button>
                </CardContent>
            </Card>
        </div>
    );
}
