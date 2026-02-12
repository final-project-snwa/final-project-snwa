package com.team.snwa.snwabackend.domain.exp.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LevelCalculator {
    private static final int MAX_LEVEL = 99;
    private static final double EXP_MULTIPLIER = 50.0;
    private static final double EXP_EXPONENT = 1.8;

    private final List<Long> cumulativeExpByLevel = new ArrayList<>(MAX_LEVEL + 1);

    @PostConstruct
    public void init() {
        cumulativeExpByLevel.add(0L);
        long cumulative = 0;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            long expForNextLevel = (long) (EXP_MULTIPLIER * Math.pow(level, EXP_EXPONENT));
            cumulative += expForNextLevel;
            cumulativeExpByLevel.add(cumulative);
        }
    }

    /**
     * 누적 경험치로 레벨 계산.
     * cumulativeExpByLevel[i] = 레벨 (i+1) 달성에 필요한 누적 EXP
     */
    public int calculateLevel(long totalExp) {
        int idx = Collections.binarySearch(cumulativeExpByLevel, totalExp);
        if (idx >= 0) {
            return idx + 1;  // 정확히 일치 시 해당 누적 EXP 달성 = 다음 레벨
        }
        int insertionPoint = -idx - 1;  // totalExp보다 큰 첫 번째 인덱스
        return Math.max(1, Math.min(MAX_LEVEL, insertionPoint));
    }

    /** 현재 레벨에서 다음 레벨까지 필요한 경험치 (level은 1-based) */
    public long expToNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return 0;
        long currentCumulative = cumulativeExpByLevel.get(currentLevel - 1);
        long nextCumulative = cumulativeExpByLevel.get(currentLevel);
        return nextCumulative - currentCumulative;
    }

    /** 현재 레벨에서 이미 쌓은 경험치 (level은 1-based) */
    public long expProgressInCurrentLevel(long totalExp, int level) {
        if (level >= MAX_LEVEL) return 0;
        long levelStartExp = cumulativeExpByLevel.get(level - 1);
        return Math.max(0, totalExp - levelStartExp);
    }
}
