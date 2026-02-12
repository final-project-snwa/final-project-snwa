import { createContext, useContext, useState, ReactNode } from 'react';
import { ExpToast } from '../components/ExpToast';

export interface ExpGrantInfo {
  expGained: number;
  levelUp: boolean;
  newLevel: number;
}

interface ExpToastContextType {
  showExpToast: (info: ExpGrantInfo) => void;
}

const ExpToastContext = createContext<ExpToastContextType | undefined>(undefined);

export function ExpToastProvider({ children }: { children: ReactNode }) {
  const [info, setInfo] = useState<ExpGrantInfo | null>(null);
  const showExpToast = (i: ExpGrantInfo) => setInfo(i);
  const clear = () => setInfo(null);

  return (
    <ExpToastContext.Provider value={{ showExpToast }}>
      {children}
      {info && (
        <ExpToast
          expGained={info.expGained}
          levelUp={info.levelUp}
          newLevel={info.newLevel}
          onClose={clear}
        />
      )}
    </ExpToastContext.Provider>
  );
}

export function useExpToast() {
  const ctx = useContext(ExpToastContext);
  return ctx;
}
