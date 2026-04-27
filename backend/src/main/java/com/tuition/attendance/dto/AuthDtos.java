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
            @NotBlank String firstName,
            String middleName,
            String lastName,
            @NotNull StudentClass studentClass,
            @Email @NotBlank String email,
            @NotBlank Long mobile
    ) { }

    public record LoginRequest(
            @NotBlank String name,
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


    public record AttendanceReportItem(
            Long id,
            String studentName,
            StudentClass studentClass,
            LocalDate attendanceDate,
            String status,
            LocalDateTime scannedAt
    ) { }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @Size(min = 6, max = 100) String newPassword
    ) { }

}
