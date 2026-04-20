package com.tuition.attendance.dto;

import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String name,
            @NotNull StudentClass studentClass,
            @Email @NotBlank String email
    ) { }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) { }

    public record RegistrationResponse(String message) { }

    public record AuthResponse(String token, UserSummary user) { }

    public record UserSummary(
            Long id,
            String name,
            String email,
            Role role,
            StudentClass studentClass,
            boolean approved,
            boolean passwordChangeRequired
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

    public record StudentDashboardResponse(UserSummary student, List<AttendanceView> attendanceHistory, double attendancePercentage) { }

    public record FingerprintRegistrationRequest(@NotBlank String fingerprintId, String provider) { }

    public record FingerprintScanRequest(@NotBlank String fingerprintId) { }

    public record AttendanceMarkResponse(String message, AttendanceView attendance) { }

    public record StudentListItem(
            Long id,
            String name,
            String email,
            StudentClass studentClass,
            boolean approved,
            boolean fingerprintRegistered,
            double attendancePercentage
    ) { }

    public record PendingRegistrationItem(Long id, String name, String email, StudentClass studentClass) { }

    public record ApprovalResponse(String message) { }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @Size(min = 6, max = 100) String newPassword
    ) { }

    public record DailyAttendancePoint(LocalDate date, long presentCount) { }

    public record MonthlyAttendancePoint(String month, long presentCount) { }

    public record AdminAnalyticsResponse(List<DailyAttendancePoint> dailyAttendance, List<MonthlyAttendancePoint> monthlyAttendance, List<StudentListItem> studentWiseAttendance) { }
}
