package com.tuition.attendance.controller.admin;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.dto.StudentDtos;
import com.tuition.attendance.service.admin.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/students")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentDtos.StudentListItem> students() {
        return dashboardService.getAnalytics().studentWiseAttendance();
    }
}
