package com.tuition.attendance.service;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.AttendanceRecord;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import com.tuition.attendance.model.User;
import com.tuition.attendance.repository.AttendanceRecordRepository;
import com.tuition.attendance.repository.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final AttendanceRecordRepository attendanceRepository;
    private final UserRepository userRepository;
    private final FingerprintService fingerprintService;

    public AttendanceService(AttendanceRecordRepository attendanceRepository, UserRepository userRepository, FingerprintService fingerprintService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.fingerprintService = fingerprintService;
    }

    public AuthDtos.AttendanceMarkResponse markByFingerprint(AuthDtos.FingerprintScanRequest request) {
        User student = fingerprintService.resolveStudent(request.fingerprintId());
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only students can mark attendance");
        }
        if (!student.isApproved()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Student is not approved for attendance yet");
        }

        LocalDate today = LocalDate.now();
        attendanceRepository.findByStudentIdAndAttendanceDate(student.getId(), today).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Attendance already marked for today");
        });

        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(student);
        record.setAttendanceDate(today);
        record.setFingerprintId(request.fingerprintId());
        attendanceRepository.save(record);

        return new AuthDtos.AttendanceMarkResponse("Attendance marked successfully", Mapper.toAttendanceView(record));
    }

    public AuthDtos.StudentDashboardResponse getStudentDashboard(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        return new AuthDtos.StudentDashboardResponse(
                Mapper.toUserSummary(student),
                attendanceRepository.findStudentHistory(studentId).stream().map(Mapper::toAttendanceView).toList(),
                calculateAttendancePercentage(studentId)
        );
    }

    public List<AttendanceRecord> searchRecords(Long studentId, StudentClass studentClass, LocalDate date) {
        return attendanceRepository.search(studentId, studentClass, date);
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

    public AuthDtos.AdminAnalyticsResponse getAnalytics() {
        LocalDate today = LocalDate.now();
        List<AuthDtos.DailyAttendancePoint> daily = attendanceRepository.countDailyBetween(today.minusDays(14), today)
                .stream()
                .map(row -> new AuthDtos.DailyAttendancePoint((LocalDate) row[0], (Long) row[1]))
                .toList();

        List<AuthDtos.MonthlyAttendancePoint> monthly = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            long count = attendanceRepository.countBetween(month.atDay(1), month.atEndOfMonth());
            monthly.add(new AuthDtos.MonthlyAttendancePoint(month.format(MONTH_FORMATTER), count));
        }

        List<AuthDtos.StudentListItem> studentWise = userRepository.findByRoleAndApproved(Role.STUDENT, true).stream()
                .map(student -> new AuthDtos.StudentListItem(
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getStudentClass(),
                        student.isApproved(),
                        fingerprintService.hasFingerprint(student),
                        calculateAttendancePercentage(student.getId())
                ))
                .toList();

        return new AuthDtos.AdminAnalyticsResponse(daily, monthly, studentWise);
    }
}
