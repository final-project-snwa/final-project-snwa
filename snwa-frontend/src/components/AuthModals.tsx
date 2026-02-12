import { useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useExpToast } from '../contexts/ExpToastContext';
import { AttendanceRewardModal } from './AttendanceRewardModal';

/** 출석 보상 모달 + 경험치 토스트 (로그인 시 표시) */
export function AuthModals() {
  const { expGrantInfo, clearExpGrantInfo } = useAuth();
  const expToast = useExpToast();

  useEffect(() => {
    if (expGrantInfo && expToast) {
      expToast.showExpToast(expGrantInfo);
      clearExpGrantInfo();
    }
  }, [expGrantInfo, expToast, clearExpGrantInfo]);

  return <AttendanceRewardModal />;
}
