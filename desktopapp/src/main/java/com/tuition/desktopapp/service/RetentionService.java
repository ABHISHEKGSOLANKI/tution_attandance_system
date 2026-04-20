package com.tuition.desktopapp.service;

import com.tuition.desktopapp.repository.AttendanceRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetentionService {

    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);
    private final AttendanceRepository attendanceRepository;

    public RetentionService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public void cleanupAttendanceOlderThanDays(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        attendanceRepository.deleteByTimestampBefore(cutoff);
        log.info("Attendance cleanup completed for records older than {} days", days);
    }
}
