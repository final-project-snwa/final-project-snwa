package com.team.snwa.snwabackend.domain.exp.service;

import com.team.snwa.snwabackend.domain.exp.entity.AttendanceRecord;
import com.team.snwa.snwabackend.domain.exp.entity.ExpGrantHistory;
import com.team.snwa.snwabackend.domain.exp.entity.UserExp;
import com.team.snwa.snwabackend.domain.exp.repository.AttendanceRecordRepository;
import com.team.snwa.snwabackend.domain.exp.repository.ExpGrantHistoryRepository;
import com.team.snwa.snwabackend.domain.exp.repository.UserExpRepository;
import com.team.snwa.snwabackend.domain.exp.util.LevelCalculator;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpGrantService {
    private final UserExpRepository userExpRepository;
    private final ExpGrantHistoryRepository expGrantHistoryRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final LevelCalculator levelCalculator;

    private static final int ATTENDANCE_BASE_EXP = 20;
    private static final int ATTENDANCE_STREAK_BONUS_PER_DAY = 5;
    private static final int ATTENDANCE_STREAK_BONUS_MAX = 35;
    private static final int COMMENT_CREATE_EXP = 10;
    private static final int COMMENT_DAILY_LIMIT = 20;
    private static final int COIN_SPEND_EXP_PER_COIN = 200;

    /**
     * 코인 사용 경험치 지급 (코인 1개당 20 EXP)
     * @param externalRef spend의 externalRef (유니크, 중복 방지용)
     */
    @Transactional
    public ExpGrantInfo grantCoinSpend(Long userId, long coinAmount, String externalRef) {
        if (coinAmount <= 0) return null;
        String grantKey = "COIN_SPEND:" + externalRef;
        if (expGrantHistoryRepository.existsByUserIdAndGrantKey(userId, grantKey)) {
            return null;
        }
        int totalExp = (int) (coinAmount * COIN_SPEND_EXP_PER_COIN);
        ExpGrantHistory history = ExpGrantHistory.builder()
                .userId(userId)
                .grantKey(grantKey)
                .expAmount(totalExp)
                .grantType("COIN_SPEND")
                .createdAt(LocalDateTime.now())
                .build();
        expGrantHistoryRepository.save(history);
        return applyExpAndGetInfo(userId, totalExp);
    }

    /**
     * 출석 경험치 지급 (로그인 시 호출)
     */
    @Transactional
    public ExpGrantInfo grantAttendance(Long userId) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(kst);
        String grantKey = "ATTENDANCE:" + today.toString();

        if (expGrantHistoryRepository.existsByUserIdAndGrantKey(userId, grantKey)) {
            return null;
        }

        int consecutiveDays = 1;
        var latest = attendanceRecordRepository.findLatestByUserId(userId);
        if (latest.isPresent()) {
            LocalDate lastDate = latest.get().getAttendanceDate();
            if (lastDate.equals(today.minusDays(1))) {
                consecutiveDays = latest.get().getConsecutiveDays() + 1;
            }
        }

        int baseExp = ATTENDANCE_BASE_EXP;
        int streakBonus = Math.min(consecutiveDays * ATTENDANCE_STREAK_BONUS_PER_DAY, ATTENDANCE_STREAK_BONUS_MAX);
        int totalExp = baseExp + streakBonus;

        AttendanceRecord record = AttendanceRecord.builder()
                .userId(userId)
                .attendanceDate(today)
                .consecutiveDays(consecutiveDays)
                .build();
        attendanceRecordRepository.save(record);

        ExpGrantHistory history = ExpGrantHistory.builder()
                .userId(userId)
                .grantKey(grantKey)
                .expAmount(totalExp)
                .grantType("ATTENDANCE")
                .createdAt(LocalDateTime.now())
                .build();
        expGrantHistoryRepository.save(history);

        return applyExpAndGetInfo(userId, totalExp);
    }

    /**
     * 댓글 작성 경험치 지급
     * grant_key에 commentId 사용 (댓글마다 유니크하여 10개 이상 등록 시 충돌 방지)
     */
    @Transactional
    public ExpGrantInfo grantCommentCreate(Long userId, Long commentId) {
        LocalDate today = LocalDate.now();
        long count = expGrantHistoryRepository.countByUserIdAndGrantTypeAndDate(userId, "COMMENT_CREATE", today);
        if (count >= COMMENT_DAILY_LIMIT) return null;

        String grantKey = "COMMENT_CREATE:" + commentId;
        if (expGrantHistoryRepository.existsByUserIdAndGrantKey(userId, grantKey)) {
            return null;
        }
        ExpGrantHistory history = ExpGrantHistory.builder()
                .userId(userId)
                .grantKey(grantKey)
                .expAmount(COMMENT_CREATE_EXP)
                .grantType("COMMENT_CREATE")
                .createdAt(LocalDateTime.now())
                .build();
        expGrantHistoryRepository.save(history);
        return applyExpAndGetInfo(userId, COMMENT_CREATE_EXP);
    }

    private ExpGrantInfo applyExpAndGetInfo(Long userId, int amount) {
        UserExp userExp = userExpRepository.findByUserId(userId)
                .orElseGet(() -> userExpRepository.save(UserExp.builder().userId(userId).build()));

        int oldLevel = userExp.getLevel();
        userExp.addExp(amount);
        int newLevel = levelCalculator.calculateLevel(userExp.getTotalExp());
        userExp.setLevel(newLevel);
        userExpRepository.save(userExp);

        return new ExpGrantInfo(amount, oldLevel < newLevel, newLevel);
    }

    public record ExpGrantInfo(int expGained, boolean levelUp, int newLevel) {}
}
