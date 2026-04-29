package com.tuition.attendance.controller.student;

import com.tuition.attendance.dto.AttendanceRequestDTO;
import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.security.UserPrincipal;
import com.tuition.attendance.service.admin.AttendanceRequestService;
import com.tuition.attendance.service.admin.AttendanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final AttendanceService attendanceService;
    private final AttendanceRequestService attendanceRequestService;

    public StudentController(AttendanceService attendanceService,
                             AttendanceRequestService attendanceRequestService) {
        this.attendanceService = attendanceService;
        this.attendanceRequestService = attendanceRequestService;
    }

    @GetMapping("/dashboard")
    public AuthDtos.StudentDashboardResponse dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return attendanceService.getStudentDashboard(principal.getId());
    }

    @PostMapping("/attendance-requests")
    public AttendanceRequestDTO createAttendanceRequest(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody AttendanceRequestDTO request) {
        return attendanceRequestService.createRequest(principal, request);
    }

    @GetMapping("/attendance-requests")
    public List<AttendanceRequestDTO> myAttendanceRequests(@AuthenticationPrincipal UserPrincipal principal) {
        return attendanceRequestService.studentRequests(principal);
    }
}
