package com.tuition.attendance.service;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.User;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.security.JwtService;
import com.tuition.attendance.security.UserPrincipal;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public AuthDtos.RegistrationResponse registerStudent(AuthDtos.RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(email);
        user.setStudentClass(request.studentClass());
        user.setRole(Role.STUDENT);
        user.setApproved(false);
        user.setPasswordChangeRequired(false);
        user.setPasswordHash(null);
        userRepository.save(user);

        return new AuthDtos.RegistrationResponse("Registration request submitted. Please wait for admin approval.");
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(email)
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

    public List<AuthDtos.PendingRegistrationItem> pendingRegistrations() {
        return userRepository.findByRoleAndApproved(Role.STUDENT, false).stream()
                .map(user -> new AuthDtos.PendingRegistrationItem(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getStudentClass()
                ))
                .toList();
    }

    public AuthDtos.ApprovalResponse approveStudent(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        if (user.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only student requests can be approved");
        }
        if (user.isApproved()) {
            throw new ApiException(HttpStatus.CONFLICT, "Student is already approved");
        }

        String generatedPassword = generatePassword(10);
        user.setApproved(true);
        user.setActive(true);
        user.setPasswordChangeRequired(true);
        user.setPasswordHash(passwordEncoder.encode(generatedPassword));
        userRepository.save(user);

        emailService.sendCredentials(user.getEmail(), user.getName(), generatedPassword);
        return new AuthDtos.ApprovalResponse("Registration approved and credentials sent to student email");
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

    private String generatePassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length())));
        }
        return builder.toString();
    }
}
