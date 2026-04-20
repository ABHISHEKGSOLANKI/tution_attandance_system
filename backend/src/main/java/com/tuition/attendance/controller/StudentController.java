package com.tuition.attendance.controller;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.security.UserPrincipal;
import com.tuition.attendance.service.AttendanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final AttendanceService attendanceService;

    public StudentController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/dashboard")
    public AuthDtos.StudentDashboardResponse dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return attendanceService.getStudentDashboard(principal.getId());
    }
}
