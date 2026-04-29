package com.tuition.attendance.controller;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.dto.RegistrationRequestDTO;
import com.tuition.attendance.security.UserPrincipal;
import com.tuition.attendance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public AuthDtos.RegistrationResponse register(@Valid @ModelAttribute RegistrationRequestDTO request) {
        return authService.registerStudent(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AuthDtos.UserSummary me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.currentUser(principal);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public AuthDtos.UserSummary changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        return authService.changePassword(principal, request);
    }
}
