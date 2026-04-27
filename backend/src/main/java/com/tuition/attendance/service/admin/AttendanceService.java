package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.entities.AttendanceRecord;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.AttendanceRecordRepository;
import com.tuition.attendance.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import com.tuition.attendance.service.FingerprintService;
import com.tuition.attendance.service.Mapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {


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

    public List<AuthDtos.AttendanceReportItem> attendanceReport(LocalDate date, LocalDate startDate, LocalDate endDate, StudentClass studentClass, String name) {
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
                .map(record -> new AuthDtos.AttendanceReportItem(
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


}
