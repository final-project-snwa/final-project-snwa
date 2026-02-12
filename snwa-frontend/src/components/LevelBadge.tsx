import { memo } from 'react';

const LEVEL_RANGES = [
  { min: 1, max: 10, base: '#4a4a4a', end: '#6a6a6a', glow: false },
  { min: 11, max: 20, base: '#15803d', end: '#22c55e', glow: false },
  { min: 21, max: 40, base: '#2563eb', end: '#60a5fa', glow: false },
  { min: 41, max: 60, base: '#6d28d9', end: '#a78bfa', glow: true },
  { min: 61, max: 80, base: '#c2410c', end: '#fb923c', glow: true },
  { min: 81, max: 99, base: '#dc2626', end: '#fbbf24', glow: true },
] as const;

function getBadgeStyle(level: number) {
  const range = LEVEL_RANGES.find((r) => level >= r.min && level <= r.max) ?? LEVEL_RANGES[0];
  const t = (level - range.min) / (range.max - range.min + 1);
  const r = Math.round(
    parseInt(range.base.slice(1, 3), 16) * (1 - t) + parseInt(range.end.slice(1, 3), 16) * t
  );
  const g = Math.round(
    parseInt(range.base.slice(3, 5), 16) * (1 - t) + parseInt(range.end.slice(3, 5), 16) * t
  );
  const b = Math.round(
    parseInt(range.base.slice(5, 7), 16) * (1 - t) + parseInt(range.end.slice(5, 7), 16) * t
  );
  const color = `rgb(${r},${g},${b})`;
  return {
    backgroundColor: color,
    boxShadow: range.glow ? `0 0 6px ${color}` : undefined,
    color: '#fff',
    padding: '2px 6px',
    borderRadius: '4px',
    fontSize: '11px',
    fontWeight: 600,
  };
}

interface LevelBadgeProps {
  level: number;
  className?: string;
}

export const LevelBadge = memo(function LevelBadge({ level, className }: LevelBadgeProps) {
  const style = getBadgeStyle(level);
  return (
    <span className={className} style={style} title={`레벨 ${level}`}>
      Lv.{level}
    </span>
  );
});
