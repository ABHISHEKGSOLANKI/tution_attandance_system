package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.AttendanceDtos;
import com.tuition.attendance.dto.StudentDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.AttendanceRecordRepository;
import com.tuition.attendance.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRepository;

    public DashboardService(UserRepository userRepository, AttendanceRecordRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public StudentDtos.AdminAnalyticsResponse getAnalytics() {
        LocalDate today = LocalDate.now();
        List<AttendanceDtos.DailyAttendancePoint> daily = attendanceRepository.countDailyBetween(today.minusDays(14), today)
                .stream()
                .map(row -> new AttendanceDtos.DailyAttendancePoint((LocalDate) row[0], (Long) row[1]))
                .toList();

        List<AttendanceDtos.MonthlyAttendancePoint> monthly = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            long count = attendanceRepository.countBetween(month.atDay(1), month.atEndOfMonth());
            monthly.add(new AttendanceDtos.MonthlyAttendancePoint(month.format(MONTH_FORMATTER), count));
        }

        List<StudentDtos.StudentListItem> studentWise = userRepository.findByRoleAndApproved(Role.STUDENT, true).stream()
                .map(student -> new StudentDtos.StudentListItem(
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getStudentClass(),
                        student.isApproved(),
                        fingerprintService.hasFingerprint(student),
                        calculateAttendancePercentage(student.getId())
                ))
                .toList();

        return new AttendanceDtos.AdminAnalyticsResponse(daily, monthly, studentWise);
    }

    public double calculateAttendancePercentage(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        LocalDate startDate = student.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        long totalDays = Math.max(1, startDate.datesUntil(today.plusDays(1)).count());
        long presentDays = attendanceRepository.countPresentBetween(studentId, startDate, today);
        return Math.round((presentDays * 10000.0) / totalDays) / 100.0;
    }
}
