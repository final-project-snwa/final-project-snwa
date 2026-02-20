import React, { useState, useEffect } from 'react';

interface LoadingBarProps {
    value: number; // 0 ~ 100
    size?: number; // 크기 (기본 120)
    strokeWidth?: number; // 선 두께 (기본 10)
}

const LOADING_MESSAGES = [
    "해설위원들이 기사 분석을 위해 전술판을 짜고 있습니다... 잠시만요!",
    "지금 AI 심판이 비디오 판독(VAR)으로 기사를 정밀 분석 중입니다!",
    "기사 요약 엔진, 풀타임 가동 중! 추가 시간 1분 드립니다.",
    "AI가 라커룸에서 번역 전술을 지시하고 있습니다. 곧 출전합니다!",
    "현지 기사를 우리말로 완벽하게 '빌드업' 하는 중입니다...!",
    "지금 AI가 스포츠 기사를 3줄 요약하려고 굵은 땀방울을 흘리고 있어요.",
    "현지 기사의 핵심만 쏙쏙! AI 스카우터가 유망한 문장들을 선별 중입니다."
];

const LoadingBar: React.FC<LoadingBarProps> = ({ value, size = 120, strokeWidth = 10 }) => {
    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (value / 100) * circumference;

    const [message, setMessage] = useState("해설위원들이 기사 분석을 위해 전술판을 짜고 있습니다... 잠시만요!");

    useEffect(() => {
        // 컴포넌트 마운트 시 랜덤 메시지 선택
        const randomIndex = Math.floor(Math.random() * LOADING_MESSAGES.length);
        setMessage(LOADING_MESSAGES[randomIndex]);
    }, []);

    return (
        <div className="flex flex-col items-center justify-center space-y-4">
            <div className="relative flex items-center justify-center" style={{ width: size, height: size }}>
                <svg className="transform -rotate-90 w-full h-full">
                    {/* 배경 원 (회색) */}
                    <circle
                        cx="50%"
                        cy="50%"
                        r={radius}
                        stroke="#e5e7eb" // gray-200
                        strokeWidth={strokeWidth}
                        fill="transparent"
                    />
                    {/* 진행률 원 (파란색) */}
                    <circle
                        cx="50%"
                        cy="50%"
                        r={radius}
                        stroke="#2563eb" // blue-600
                        strokeWidth={strokeWidth}
                        fill="transparent"
                        strokeDasharray={circumference}
                        strokeDashoffset={offset}
                        strokeLinecap="round" // 끝을 둥글게
                        className="transition-all duration-300 ease-out"
                    />
                </svg>
                {/* 가운데 퍼센트 텍스트 */}
                <div className="absolute text-xl font-bold text-blue-600">
                    {Math.round(value)}%
                </div>
            </div>
            <p className="text-gray-600 font-medium animate-pulse text-center">
                {message}
            </p>
        </div>
    );
};

export default LoadingBar;
