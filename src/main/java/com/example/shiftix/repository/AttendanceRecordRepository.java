package com.example.shiftix.repository;

import com.example.shiftix.entity.AttendanceRecord;
import com.example.shiftix.entity.AttendanceType;
import com.example.shiftix.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByUserOrderByTimestampDesc(User user);

    List<AttendanceRecord> findByUserAndTimestampBetweenOrderByTimestampDesc(
        User user, LocalDateTime start, LocalDateTime end);

    Optional<AttendanceRecord> findTopByUserOrderByTimestampDesc(User user);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user = :user AND ar.type = :type " +
           "AND DATE(ar.timestamp) = DATE(:date) ORDER BY ar.timestamp DESC")
    List<AttendanceRecord> findByUserAndTypeAndDate(
        @Param("user") User user,
        @Param("type") AttendanceType type,
        @Param("date") LocalDateTime date);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE DATE(ar.timestamp) = DATE(:date)")
    List<AttendanceRecord> findByDate(@Param("date") LocalDateTime date);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.timestamp BETWEEN :start AND :end")
    List<AttendanceRecord> findByDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
