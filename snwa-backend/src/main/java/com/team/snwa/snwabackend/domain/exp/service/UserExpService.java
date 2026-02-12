package com.team.snwa.snwabackend.domain.exp.service;

import com.team.snwa.snwabackend.domain.exp.dto.UserExpSummaryDto;
import com.team.snwa.snwabackend.domain.exp.entity.UserExp;
import com.team.snwa.snwabackend.domain.exp.repository.UserExpRepository;
import com.team.snwa.snwabackend.domain.exp.util.LevelCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExpService {
    private final UserExpRepository userExpRepository;
    private final LevelCalculator levelCalculator;

    public UserExpSummaryDto getMyExp(Long userId) {
        UserExp userExp = userExpRepository.findByUserId(userId)
                .orElseGet(() -> UserExp.builder().userId(userId).totalExp(0).level(1).build());

        long totalExp = userExp.getTotalExp();
        int level = levelCalculator.calculateLevel(totalExp);
        long expToNext = levelCalculator.expToNextLevel(level);
        long expProgress = levelCalculator.expProgressInCurrentLevel(totalExp, level);

        return new UserExpSummaryDto(level, totalExp, expToNext, expProgress);
    }
}
