package com.team.snwa.snwabackend.domain.wallet.service;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType;
import com.team.snwa.snwabackend.domain.wallet.repository.CoinTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class    CoinTransactionService {

    private final CoinTransactionRepository coinTransactionRepository;

    //중복 트렌잭션 여부 판별
    public boolean isDuplicate(Long userId, String externalRef) {
        return coinTransactionRepository.existsByUserIdAndExternalRef(userId, externalRef);
    }

    //출석 보상 지급 여부 판별
    public boolean hasTodayAttendanceReward(Long userId) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(kst);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return coinTransactionRepository.existsByUserIdAndTypeAndCreatedDateBetween(
                userId,
                CoinTransactionType.ATTENDANCE_REWARD,
                start,
                end
        );
    }

    //트랙잭션 저장
    public CoinTransaction save(CoinTransaction tx) {
        return coinTransactionRepository.save(tx);
    }

    //내역 조회
    public List<CoinTransaction> getHistory(Long userId) {
        return coinTransactionRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    //해당 기사에 코인 사용 내역이 존재하는지 확인
    public boolean hasUsedCoinForArticle(Long userId, Long articleId) {
        String externalRef = "ARTICLE_" + articleId;

        return coinTransactionRepository.existsByUserIdAndTypeAndExternalRef(
                userId,
                CoinTransactionType.SPEND,
                externalRef
        );
    }
}
