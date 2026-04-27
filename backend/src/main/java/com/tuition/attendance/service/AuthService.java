package com.tuition.attendance.service;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.security.JwtService;
import com.tuition.attendance.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.RegistrationResponse registerStudent(AuthDtos.RegisterRequest request) {
        String username = request.name().toLowerCase().trim();
        if (userRepository.existsByNameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setFirstName(request.firstName());
        user.setMiddleName(request.middleName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setStudentClass(request.studentClass());
        user.setRole(Role.STUDENT);
        user.setApproved(false);
        user.setPasswordChangeRequired(false);
        user.setPasswordHash(null);
        user.setMobile(request.mobile());
        userRepository.save(user);

        return new AuthDtos.RegistrationResponse("Registration request submitted. Please wait for admin approval.");
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String username = request.name().toLowerCase().trim();
        User user = userRepository.findByNameIgnoreCase(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isApproved()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your registration request is still pending admin approval");
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthDtos.AuthResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()), Mapper.toUserSummary(user));
    }
    public AuthDtos.UserSummary currentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .map(Mapper::toUserSummary)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
    public AuthDtos.UserSummary changePassword(UserPrincipal principal, AuthDtos.ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
        return Mapper.toUserSummary(user);
    }
}
