import { useEffect, useState } from 'react';

interface ExpToastProps {
  expGained: number;
  levelUp?: boolean;
  newLevel?: number;
  onClose: () => void;
  duration?: number;
}

export function ExpToast({
  expGained,
  levelUp,
  newLevel,
  onClose,
  duration = 2500,
}: ExpToastProps) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const t = setTimeout(() => {
      setVisible(false);
      onClose();
    }, duration);
    return () => clearTimeout(t);
  }, [duration, onClose]);

  if (!visible) return null;

  return (
    <div
      className="fixed top-6 left-1/2 -translate-x-1/2 z-[9999] px-6 py-3 rounded-lg font-semibold text-white shadow-lg animate-exp-toast-in"
      style={{
        background: 'linear-gradient(135deg, #10b981, #059669)',
      }}
    >
      +{expGained} EXP!
      {levelUp && newLevel && (
        <span className="ml-2 opacity-90">레벨 {newLevel} 달성!</span>
      )}
    </div>
  );
}
