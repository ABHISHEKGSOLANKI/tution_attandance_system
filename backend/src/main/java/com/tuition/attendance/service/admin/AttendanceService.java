package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.AttendanceDtos;
import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.dto.StudentDtos;
import com.tuition.attendance.entities.AttendanceRecord;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.AttendanceType;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import com.tuition.attendance.repository.AttendanceRecordRepository;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.service.Mapper;
import com.tuition.attendance.service.biometric.FaceService;
import com.tuition.attendance.service.biometric.FingerprintService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AttendanceRecordRepository attendanceRepository;
    private final UserRepository userRepository;
    private final FingerprintService fingerprintService;
    private final FaceService faceService;

    public AttendanceService(AttendanceRecordRepository attendanceRepository,
                             UserRepository userRepository,
                             FingerprintService fingerprintService,
                             FaceService faceService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.fingerprintService = fingerprintService;
        this.faceService = faceService;
    }

    public AttendanceDtos.AttendanceMarkResponse markAttendance(AttendanceDtos.BiometricScanRequest request) {
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
        record.setAttendanceType(AttendanceType.FINGERPRINT);
        attendanceRepository.save(record);

        return new AttendanceDtos.AttendanceMarkResponse("Attendance marked successfully", Mapper.toAttendanceView(record));
    }

    public AttendanceDtos.AttendanceMarkResponse markAttendanceByFace(AttendanceDtos.FaceAttendanceMarkRequest request) {
        User student = resolveStudentForFaceAttendance(request.studentId());
        if (student.getRole() != Role.STUDENT) {
            return new AttendanceDtos.AttendanceMarkResponse("Only students can mark attendance", null);
        }
        if (!student.isApproved()) {
            return new AttendanceDtos.AttendanceMarkResponse("Student is not approved for attendance yet", null);
        }

        LocalDate attendanceDate = request.timestamp().toLocalDate();

        Optional<AttendanceRecord> existingAttendance =
                attendanceRepository.findByStudentIdAndAttendanceDate(
                        student.getId(),
                        attendanceDate
                );

        if (existingAttendance.isPresent()) {
            return new AttendanceDtos.AttendanceMarkResponse("Attendance already marked for today",
                    Mapper.toAttendanceView(existingAttendance.get())
            );
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(student);
        record.setAttendanceDate(attendanceDate);
        record.setAttendanceType(AttendanceType.FACE);
        record.setScannedAt(request.timestamp());
        attendanceRepository.save(record);

        return new AttendanceDtos.AttendanceMarkResponse("Attendance marked successfully", Mapper.toAttendanceView(record));
    }

    private User resolveStudentForFaceAttendance(String studentIdentifier) {
        String normalized = studentIdentifier == null ? "" : studentIdentifier.trim();
        if (normalized.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Student identifier is required");
        }

        try {
            Long numericId = Long.parseLong(normalized);
            return userRepository.findById(numericId)
                    .orElseGet(() -> userRepository.findByAdmissionIdIgnoreCase(normalized)
                            .orElseGet(() -> userRepository.findByUsernameIgnoreCase(normalized)
                                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"))));
        } catch (NumberFormatException ignored) {
            return userRepository.findByAdmissionIdIgnoreCase(normalized)
                    .orElseGet(() -> userRepository.findByUsernameIgnoreCase(normalized)
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found")));
        }
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

    public List<AttendanceDtos.AttendanceReportItem> attendanceReport(LocalDate date, LocalDate startDate, LocalDate endDate, StudentClass studentClass, String name) {
        LocalDate effectiveStart = startDate;
        LocalDate effectiveEnd = endDate;
        if (date != null) {
            effectiveStart = date;
            effectiveEnd = date;
        }
        final LocalDate startFilter = effectiveStart;
        final LocalDate endFilter = effectiveEnd;
        String normalizedName = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);

        return attendanceRepository.findAll().stream()
                .filter(record -> studentClass == null || record.getStudent().getStudentClass() == studentClass)
                .filter(record -> startFilter == null || !record.getAttendanceDate().isBefore(startFilter))
                .filter(record -> endFilter == null || !record.getAttendanceDate().isAfter(endFilter))
                .filter(record -> normalizedName.isBlank()
                        || record.getStudent().getName().toLowerCase(Locale.ROOT).contains(normalizedName))
                .sorted((left, right) -> {
                    int byDate = right.getAttendanceDate().compareTo(left.getAttendanceDate());
                    if (byDate != 0) {
                        return byDate;
                    }
                    return left.getStudent().getName().compareToIgnoreCase(right.getStudent().getName());
                })
                .map(record -> new AttendanceDtos.AttendanceReportItem(
                        record.getId(),
                        record.getStudent().getName(),
                        record.getStudent().getStudentClass(),
                        record.getAttendanceDate(),
                        record.getStatus().name(),
                        record.getScannedAt()
                ))
                .toList();
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

    public AttendanceDtos.AdminAnalyticsResponse getAnalytics() {
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
                        faceService.hasFaceRegistered(student),
                        calculateAttendancePercentage(student.getId()),
                        student.getUsername()
                ))
                .toList();

        return new AttendanceDtos.AdminAnalyticsResponse(daily, monthly, studentWise);
    }
}
