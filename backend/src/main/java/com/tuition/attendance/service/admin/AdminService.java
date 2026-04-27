package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.AdminDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class AdminService {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    public AdminDtos.ApprovalResponse rejectStudent(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        if (user.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only student requests can be rejected");
        }
        if (user.isApproved()) {
            throw new ApiException(HttpStatus.CONFLICT, "Approved students cannot be rejected from pending requests");
        }

        userRepository.delete(user);
        return new AdminDtos.ApprovalResponse("Registration request rejected");
    }

    public AdminDtos.ApprovalResponse approveStudent(Long studentId) {
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
        return new AdminDtos.ApprovalResponse("Registration approved and credentials sent to student email");
    }

    private String generatePassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length())));
        }
        return builder.toString();
    }

    public List<AdminDtos.PendingRegistrationItem> pendingRegistrations() {
        return userRepository.findByRoleAndApproved(Role.STUDENT, false).stream()
                .map(user -> new AdminDtos.PendingRegistrationItem(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getStudentClass()
                ))
                .toList();
    }
}
