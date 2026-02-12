import { useAuth } from '../contexts/AuthContext';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from './ui/dialog';
import { Coins } from 'lucide-react';

export function AttendanceRewardModal() {
  const { attendanceRewardJustGiven, clearAttendanceRewardFlag } = useAuth();

  const handleOpenChange = (open: boolean) => {
    if (!open) clearAttendanceRewardFlag();
  };

  return (
    <Dialog open={attendanceRewardJustGiven} onOpenChange={handleOpenChange}>
      <DialogContent className="bg-gradient-to-br from-amber-50 via-white to-yellow-50 border-2 border-amber-200 shadow-xl shadow-amber-200/40 overflow-visible duration-300">
        <DialogHeader className="text-center space-y-4 pt-4">
          {/* 코인 아이콘 + 애니메이션 */}
          <div className="flex justify-center">
            <div className="relative">
              <div className="absolute inset-0 bg-amber-400/30 rounded-full blur-xl scale-150 animate-pulse" />
              <div className="relative flex items-center justify-center w-20 h-20 rounded-full bg-gradient-to-br from-amber-400 to-yellow-600 text-white shadow-lg animate-coin-pop">
                <Coins className="w-10 h-10" strokeWidth={2} />
              </div>
            </div>
          </div>
          <div className="space-y-1">
            <DialogTitle className="text-xl font-bold text-gray-900">
              출석 보상 지급
            </DialogTitle>
            <DialogDescription className="text-base text-gray-600">
              오늘의 출석 보상으로 <span className="font-semibold text-amber-600">1코인</span>이 지급되었습니다.
            </DialogDescription>
          </div>
        </DialogHeader>
      </DialogContent>
    </Dialog>
  );
}
