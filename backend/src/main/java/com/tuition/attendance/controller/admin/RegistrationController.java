package com.tuition.attendance.controller.admin;

import com.tuition.attendance.dto.ApprovalResponseDTO;
import com.tuition.attendance.dto.PendingRegistrationDTO;
import com.tuition.attendance.service.admin.RegistrationService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/pending-registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PendingRegistrationDTO> pendingRegistrations() {
        return registrationService.pendingRegistrations();
    }

    @PostMapping("/pending-registrations/{registrationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalResponseDTO approveRegistration(@PathVariable Long registrationId) {
        return registrationService.approveStudent(registrationId);
    }

    @PostMapping("/pending-registrations/{registrationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalResponseDTO rejectRegistration(@PathVariable Long registrationId) {
        return registrationService.rejectStudent(registrationId);
    }
}
