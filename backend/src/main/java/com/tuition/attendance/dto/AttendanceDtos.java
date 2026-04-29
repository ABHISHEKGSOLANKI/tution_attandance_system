package com.tuition.attendance.dto;

import com.tuition.attendance.model.StudentClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceDtos {

    public record DailyAttendancePoint(LocalDate date, long presentCount) { }

    public record MonthlyAttendancePoint(String month, long presentCount) { }

    public record AdminAnalyticsResponse(List<DailyAttendancePoint> dailyAttendance, List<MonthlyAttendancePoint> monthlyAttendance, List<StudentDtos.StudentListItem> studentWiseAttendance) { }

    public record BiometricScanRequest(@NotBlank String fingerprintId) { }

    public record FaceAttendanceMarkRequest(
            @NotBlank String studentId,
            @NotNull LocalDateTime timestamp
    ) { }

    public record AttendanceMarkResponse(String message, AttendanceView attendance) { }

    public record AttendanceReportItem(
            Long id,
            String studentName,
            StudentClass studentClass,
            LocalDate attendanceDate,
            String status,
            LocalDateTime scannedAt
    ) { }

    public record AttendanceView(
            Long id,
            Long studentId,
            String studentName,
            StudentClass studentClass,
            LocalDate attendanceDate,
            String fingerprintId,
            LocalDateTime scannedAt
    ) { }
}
