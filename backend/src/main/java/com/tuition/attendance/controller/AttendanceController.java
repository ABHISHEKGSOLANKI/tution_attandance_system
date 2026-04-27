package com.tuition.attendance.controller;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.model.StudentClass;
import com.tuition.attendance.service.admin.AttendanceService;
import com.tuition.attendance.service.FingerprintService;
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
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final FingerprintService fingerprintService;

    public AttendanceController(AttendanceService attendanceService, FingerprintService fingerprintService) {
        this.attendanceService = attendanceService;
        this.fingerprintService = fingerprintService;
    }

    @PostMapping("/attendance/mark")
    public AuthDtos.AttendanceMarkResponse markAttendance(@Valid @RequestBody AuthDtos.FingerprintScanRequest request) {
        return attendanceService.markByFingerprint(request);
    }

    @PostMapping("/admin/students/{studentId}/fingerprint")
    @PreAuthorize("hasRole('ADMIN')")
    public void registerFingerprint(@PathVariable Long studentId,
                                    @Valid @RequestBody AuthDtos.FingerprintRegistrationRequest request) {
        fingerprintService.registerFingerprint(studentId, request);
    }



    @GetMapping("/admin/attendance")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthDtos.AttendanceView> attendance(@RequestParam(required = false) Long studentId,
                                                    @RequestParam(required = false) StudentClass studentClass,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.searchRecords(studentId, studentClass, date).stream().map(com.tuition.attendance.service.Mapper::toAttendanceView).toList();
    }

    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthDtos.AdminAnalyticsResponse analytics() {
        return attendanceService.getAnalytics();
    }

    @GetMapping("/attendance/report")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthDtos.AttendanceReportItem> attendanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) StudentClass studentClass,
            @RequestParam(required = false) String name) {
        return attendanceService.attendanceReport(date, startDate, endDate, studentClass, name);
    }
}
