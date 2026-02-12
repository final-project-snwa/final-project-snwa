package com.team.snwa.snwabackend.domain.exp.service;

import com.team.snwa.snwabackend.domain.exp.dto.LeaderboardEntryDto;
import com.team.snwa.snwabackend.domain.exp.entity.UserExp;
import com.team.snwa.snwabackend.domain.exp.repository.UserExpRepository;
import com.team.snwa.snwabackend.domain.exp.util.LevelCalculator;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {
    private final UserExpRepository userExpRepository;
    private final UserRepository userRepository;
    private final LevelCalculator levelCalculator;

    public List<LeaderboardEntryDto> getLeaderboard(int limit) {
        List<UserExp> list = userExpRepository.findLeaderboardAll(PageRequest.of(0, limit));
        if (list.isEmpty()) return List.of();

        Set<Long> userIds = list.stream().map(UserExp::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            UserExp u = list.get(i);
            User user = userMap.get(u.getUserId());
            int level = levelCalculator.calculateLevel(u.getTotalExp());
            result.add(new LeaderboardEntryDto(
                    i + 1, u.getUserId(),
                    user != null ? user.getNickname() : null,
                    level, u.getTotalExp()));
        }
        return result;
    }
}
