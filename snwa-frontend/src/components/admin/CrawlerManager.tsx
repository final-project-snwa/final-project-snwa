import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import CrawlerJobs from './crawler/CrawlerJobs';
import CrawlerLogs from './crawler/CrawlerLogs';
import CrawlerSystem from './crawler/CrawlerSystem';

export default function CrawlerManager() {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-medium">크롤러 관리</h3>
                <p className="text-sm text-gray-500">
                    뉴스 수집 작업을 관리하고 실행 로그 및 시스템 상태를 모니터링합니다.
                </p>
            </div>
            
            <Tabs defaultValue="jobs" className="space-y-4">
                <TabsList>
                    <TabsTrigger value="jobs">작업 관리</TabsTrigger>
                    <TabsTrigger value="logs">실행 로그</TabsTrigger>
                    <TabsTrigger value="system">시스템 상태</TabsTrigger>
                </TabsList>
                
                <TabsContent value="jobs" className="space-y-4">
                    <CrawlerJobs />
                </TabsContent>
                
                <TabsContent value="logs" className="space-y-4">
                    <CrawlerLogs />
                </TabsContent>
                
                <TabsContent value="system" className="space-y-4">
                    <CrawlerSystem />
                </TabsContent>
            </Tabs>
        </div>
    );
}
