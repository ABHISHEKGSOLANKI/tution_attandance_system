package com.tuition.desktopapp.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ApiDtos {

    public record StudentRegistrationRequest(
            @NotBlank String studentId,
            @NotBlank String name,
            @NotBlank String studentClass
    ) { }

    public record StudentRegistrationResponse(
            Long id,
            String studentId,
            String name,
            String studentClass,
            LocalDateTime createdAt
    ) { }

    public record StudentListItem(
            Long id,
            String studentId,
            String name,
            String studentClass,
            LocalDateTime createdAt
    ) { }

    public record AttendanceTriggerResponse(
            boolean matched,
            String message,
            String studentId,
            String name,
            LocalDate attendanceDate,
            LocalDateTime timestamp
    ) { }

    public record AttendanceTriggerRequest(String mockTemplate) { }

    public record AttendanceBulkItem(
            Long localRecordId,
            String studentId,
            String name,
            String studentClass,
            LocalDate attendanceDate,
            LocalDateTime timestamp
    ) { }

    public record AttendanceBulkSyncRequest(List<AttendanceBulkItem> records) { }

    public record SyncResult(
            int attempted,
            int synced,
            int remaining,
            String message
    ) { }

    public record DashboardAttendanceItem(
            String studentId,
            String name,
            String studentClass,
            LocalDate attendanceDate,
            LocalDateTime timestamp,
            boolean synced
    ) { }

    public record DashboardSummary(
            long totalPresentToday,
            long pendingSyncToday,
            String syncStatus,
            List<DashboardAttendanceItem> todayAttendance
    ) { }
}
