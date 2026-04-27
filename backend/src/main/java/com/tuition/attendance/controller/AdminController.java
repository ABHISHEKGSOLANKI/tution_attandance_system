package com.tuition.attendance.controller;

import com.tuition.attendance.dto.AdminDtos;
import com.tuition.attendance.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pending-registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminDtos.PendingRegistrationItem> pendingRegistrations() {
        return adminService.pendingRegistrations();
    }

    @PostMapping("/pending-registrations/{studentId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDtos.ApprovalResponse approveRegistration(@PathVariable Long studentId) {
        return adminService.approveStudent(studentId);
    }

    @PostMapping("/pending-registrations/{studentId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDtos.ApprovalResponse rejectRegistration(@PathVariable Long studentId) {
        return adminService.rejectStudent(studentId);
    }
}
