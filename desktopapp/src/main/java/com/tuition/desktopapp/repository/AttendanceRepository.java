package com.tuition.desktopapp.repository;

import com.tuition.desktopapp.model.AttendanceRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByStudentIdAndAttendanceDate(Long studentId, LocalDate attendanceDate);

    List<AttendanceRecord> findAllBySyncedFalseOrderByTimestampAsc();

    List<AttendanceRecord> findAllByAttendanceDateOrderByTimestampDesc(LocalDate attendanceDate);

    long countByAttendanceDate(LocalDate attendanceDate);

    long countByAttendanceDateAndSyncedFalse(LocalDate attendanceDate);

    void deleteByTimestampBefore(LocalDateTime cutoff);
}
