package com.team.snwa.snwabackend.domain.exp.repository;

import com.team.snwa.snwabackend.domain.exp.entity.AttendanceRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByUserIdAndAttendanceDate(Long userId, LocalDate date);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.userId = :userId ORDER BY ar.attendanceDate DESC")
    List<AttendanceRecord> findTopByUserIdOrderByAttendanceDateDesc(Long userId, PageRequest pageRequest);

    default Optional<AttendanceRecord> findLatestByUserId(Long userId) {
        List<AttendanceRecord> list = findTopByUserIdOrderByAttendanceDateDesc(userId, PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
