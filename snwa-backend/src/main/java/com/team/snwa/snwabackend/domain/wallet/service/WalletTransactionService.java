package com.team.snwa.snwabackend.domain.wallet.service;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import com.team.snwa.snwabackend.domain.wallet.entity.Wallet;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionStatus;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType;
import com.team.snwa.snwabackend.domain.wallet.repository.CoinTransactionRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class WalletTransactionService {
    private final WalletService walletService;
    private final CoinTransactionRepository coinTransactionRepository;
    private final CoinTransactionService coinTransactionService;

    public Long getBalance(User user){
        return walletService.getBalance(user);
    }


    //1. 코인 사용 + 거래로그 기록
    @Transactional
    public CoinTransaction spend(User user,Long amount, String externalRef) {
        Wallet wallet = walletService.getOrCreate(user);

        // 중복 발생시 에러 처리
        if (externalRef == null || externalRef.isBlank()) {
            throw new CustomException(ErrorCode.WALLET_EXTERNAL_REF_REQUIRED);
        }

        // 같은 요청이 여러 번 들어와도 1번만 차감되게 막음
        if (coinTransactionService.isDuplicate(user.getId(), externalRef)) {
            throw new CustomException(ErrorCode.WALLET_TRANSACTION_DUPLICATED);
        }

        // 1) 잔액 차감
        wallet.decrease(amount);

        // 2) 거래 로그 저장
        CoinTransaction tx = CoinTransaction.create(
                user.getId(),
                CoinTransactionType.SPEND,
                CoinTransactionStatus.SUCCESS,
                amount,
                wallet.getBalance(),
                externalRef
        );
        return coinTransactionService.save(tx);
    }

    // 2. 코인 충전 + 거래로그 기록
    @Transactional
    public CoinTransaction charge(User user, Long amount, String externalRef) {

        // 중복 발생시 에러 처리
        if (externalRef == null || externalRef.isBlank()) {
            throw new CustomException(ErrorCode.WALLET_EXTERNAL_REF_REQUIRED);
        }

        // 같은 결제 승인 콜백이 여러 번 들어와도 1번만 충전되게 막음
        if (coinTransactionService.isDuplicate(user.getId(), externalRef)) {
            throw new CustomException(ErrorCode.WALLET_TRANSACTION_DUPLICATED);
        }

        Wallet wallet = walletService.getOrCreate(user);

        // 1) 잔액 증가
        wallet.increase(amount);

        // 2) 거래 로그 저장
        CoinTransaction tx = CoinTransaction.create(
                user.getId(),
                CoinTransactionType.CHARGE,
                CoinTransactionStatus.SUCCESS,
                amount,
                wallet.getBalance(),
                externalRef
        );

        return coinTransactionService.save(tx);
    }

    // 3. 로그인 시 출석 보상 지급 (하루 1번 , 1코인)
    @Transactional
    public void giveAttendanceReward(User user) {
        giveAttendanceRewardByUserId(user.getId());
    }

    // 3-1. 로그인 시 출석 보상 지급 (userId 기반 - 새 트랜잭션용)
    @Transactional
    public boolean giveAttendanceRewardByUserId(Long userId) {

        // 1) 오늘 이미 지급했는지 확인
        if (coinTransactionService.hasTodayAttendanceReward(userId)) {
            return false; // 이미 지급됨
        }

        // 2) 지갑 조회 (userId 기반)
        Wallet wallet = walletService.getOrCreateByUserId(userId);

        // 3) 코인 1개 지급
        wallet.increase(1L);

        // 4) 거래 기록 생성
        ZoneId kst = ZoneId.of("Asia/Seoul");
        CoinTransaction tx = CoinTransaction.create(
                userId,
                CoinTransactionType.ATTENDANCE_REWARD,
                CoinTransactionStatus.SUCCESS,
                1L,
                wallet.getBalance(),
                "ATTENDANCE_" + userId + "_" + LocalDate.now(kst)
        );

        coinTransactionService.save(tx);
        return true; // 지급 완료
    }
}
