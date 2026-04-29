package com.tuition.attendance.controller.admin;

import com.tuition.attendance.dto.ApprovalResponseDTO;
import com.tuition.attendance.dto.AttendanceDtos;
import com.tuition.attendance.dto.AttendanceRequestDTO;
import com.tuition.attendance.model.StudentClass;
import com.tuition.attendance.service.admin.AttendanceRequestService;
import com.tuition.attendance.service.admin.AttendanceService;
import com.tuition.attendance.service.biometric.FingerprintService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceRequestService attendanceRequestService;
    private final FingerprintService fingerprintService;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceRequestService attendanceRequestService,
                                FingerprintService fingerprintService) {
        this.attendanceService = attendanceService;
        this.attendanceRequestService = attendanceRequestService;
        this.fingerprintService = fingerprintService;
    }

    @PostMapping("/mark")
    public AttendanceDtos.AttendanceMarkResponse markAttendance(@Valid @RequestBody AttendanceDtos.BiometricScanRequest request) {
        return attendanceService.markAttendance(request);
    }

    @PostMapping("/face-mark")
    public AttendanceDtos.AttendanceMarkResponse markFaceAttendance(@Valid @RequestBody AttendanceDtos.FaceAttendanceMarkRequest request) {
        return attendanceService.markAttendanceByFace(request);
    }

    @PostMapping("/students/{studentId}/fingerprint")
    @PreAuthorize("hasRole('ADMIN')")
    public void registerFingerprint(@PathVariable Long studentId,
                                    @Valid @RequestBody com.tuition.attendance.dto.AuthDtos.FingerprintRegistrationRequest request) {
        fingerprintService.registerFingerprint(studentId, request);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AttendanceDtos.AttendanceView> attendance(@RequestParam(required = false) Long studentId,
                                                          @RequestParam(required = false) StudentClass studentClass,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.searchRecords(studentId, studentClass, date).stream().map(com.tuition.attendance.service.Mapper::toAttendanceView).toList();
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public AttendanceDtos.AdminAnalyticsResponse analytics() {
        return attendanceService.getAnalytics();
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AttendanceDtos.AttendanceReportItem> attendanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) StudentClass studentClass,
            @RequestParam(required = false) String name) {
        return attendanceService.attendanceReport(date, startDate, endDate, studentClass, name);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AttendanceRequestDTO> attendanceRequests() {
        return attendanceRequestService.allPendingRequests();
    }

    @PostMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalResponseDTO approveAttendanceRequest(@PathVariable Long requestId) {
        return attendanceRequestService.approveRequest(requestId);
    }

    @PostMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalResponseDTO rejectAttendanceRequest(@PathVariable Long requestId) {
        return attendanceRequestService.rejectRequest(requestId);
    }
}
