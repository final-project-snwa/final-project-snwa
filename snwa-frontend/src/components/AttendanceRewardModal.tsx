import { useAuth } from '../contexts/AuthContext';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from './ui/dialog';


export function AttendanceRewardModal() {
  const { attendanceRewardJustGiven, clearAttendanceRewardFlag } = useAuth();

  const handleOpenChange = (open: boolean) => {
    if (!open) clearAttendanceRewardFlag();
  };

  return (
    <Dialog open={attendanceRewardJustGiven} onOpenChange={handleOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>출석 보상 지급</DialogTitle>
          <DialogDescription>
            오늘의 출석 보상으로 1코인이 지급되었습니다.
          </DialogDescription>
        </DialogHeader>
      </DialogContent>
    </Dialog>
  );
}
