package com.tuition.attendance.dto;

import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) { }

    public record RegistrationResponse(String message) { }

    public record AuthResponse(String token, UserSummary user) { }

    public record UserSummary(
            Long id,
            String username,
            String name,
            String email,
            Role role,
            StudentClass studentClass,
            boolean approved,
            boolean passwordChangeRequired,
            String admissionId,
            String photoUrl
    ) { }

    public record StudentDashboardResponse(UserSummary student, List<AttendanceDtos.AttendanceView> attendanceHistory, double attendancePercentage) { }

    public record FingerprintRegistrationRequest(@NotBlank String fingerprintId, String provider) { }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @Size(min = 6, max = 100) String newPassword
    ) { }
}
