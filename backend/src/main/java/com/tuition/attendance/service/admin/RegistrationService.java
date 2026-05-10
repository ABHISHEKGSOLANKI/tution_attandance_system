package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.ApprovalResponseDTO;
import com.tuition.attendance.dto.PendingRegistrationDTO;
import com.tuition.attendance.entities.PendingRegistration;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.RequestStatus;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.repository.PendingRegistrationRepository;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.service.EmailService;
import com.tuition.attendance.service.Mapper;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(PendingRegistrationRepository pendingRegistrationRepository,
                               UserRepository userRepository,
                               EmailService emailService,
                               PasswordEncoder passwordEncoder) {
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<PendingRegistrationDTO> pendingRegistrations() {
        return pendingRegistrationRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING).stream()
                .map(Mapper::toPendingRegistrationDto)
                .toList();
    }

    @Transactional
    public ApprovalResponseDTO approveStudent(Long registrationId) {
        PendingRegistration registration = pendingRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pending registration not found"));
        if (registration.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Registration request is already processed");
        }

        if (userRepository.existsByAdmissionIdIgnoreCase(registration.getAdmissionId())) {
            throw new ApiException(HttpStatus.CONFLICT, "A user with this admission ID already exists");
        }

        String username = generateUniqueUsername(registration.getFirstName(), registration.getAdmissionId());
        String generatedPassword = generatePassword(10);

        User user = new User();
        user.setName(Mapper.fullName(registration.getFirstName(), registration.getMiddleName(), registration.getLastName()));
        user.setUsername(username);
        user.setFirstName(registration.getFirstName());
        user.setMiddleName(registration.getMiddleName());
        user.setLastName(registration.getLastName());
        user.setMobile(registration.getMobile());
        user.setEmail(registration.getEmail());
        user.setAdmissionId(registration.getAdmissionId());
        user.setPhotoUrl(registration.getPhotoUrl());
        user.setStudentClass(registration.getStandard());
        user.setRole(Role.STUDENT);
        user.setApproved(true);
        user.setActive(true);
        user.setPasswordChangeRequired(true);
        user.setPasswordHash(passwordEncoder.encode(generatedPassword));
        userRepository.save(user);

        registration.setStatus(RequestStatus.APPROVED);
        registration.setApprovedAt(LocalDateTime.now());
        pendingRegistrationRepository.save(registration);

        emailService.sendCredentials(user.getEmail(), user.getName(), user.getUsername(), generatedPassword);
        return new ApprovalResponseDTO("Registration approved and credentials sent to student email");
    }

    @Transactional
    public ApprovalResponseDTO rejectStudent(Long registrationId) {
        PendingRegistration registration = pendingRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pending registration not found"));
        if (registration.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Registration request is already processed");
        }

        registration.setStatus(RequestStatus.REJECTED);
        registration.setApprovedAt(LocalDateTime.now());
        pendingRegistrationRepository.save(registration);
        return new ApprovalResponseDTO("Registration request rejected");
    }

    private String generateUniqueUsername(String firstName, String admissionId) {
        String base = firstName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "") + "_" + admissionId.trim().toLowerCase(Locale.ROOT);
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            suffix++;
            candidate = base + "_" + suffix;
        }
        return candidate;
    }

    private String generatePassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length())));
        }
        return builder.toString();
    }
}
