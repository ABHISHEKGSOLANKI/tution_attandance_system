package com.tuition.attendance.service;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.dto.RegistrationRequestDTO;
import com.tuition.attendance.entities.PendingRegistration;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.RequestStatus;
import com.tuition.attendance.repository.PendingRegistrationRepository;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.security.JwtService;
import com.tuition.attendance.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LocalStorageService localStorageService;

    public AuthService(UserRepository userRepository,
                       PendingRegistrationRepository pendingRegistrationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       LocalStorageService localStorageService) {
        this.userRepository = userRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.localStorageService = localStorageService;
    }

    public AuthDtos.RegistrationResponse registerStudent(RegistrationRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        String admissionId = request.getAdmissionId().trim();

        if (userRepository.existsByAdmissionIdIgnoreCase(admissionId) || pendingRegistrationRepository.existsByAdmissionIdIgnoreCase(admissionId)) {
            return new AuthDtos.RegistrationResponse("Admission ID already submitted");
        }

        PendingRegistration registration = new PendingRegistration();
        registration.setFirstName(request.getFirstName().trim());
        registration.setMiddleName(request.getMiddleName() == null ? null : request.getMiddleName().trim());
        registration.setLastName(request.getLastName().trim());
        registration.setMobile(request.getMobile().trim());
        registration.setCountryCode(request.getCountryCode().trim());
        registration.setEmail(email);
        registration.setAdmissionId(admissionId);
        registration.setStandard(request.getStandard());
        registration.setPhotoUrl(localStorageService.storeRegistrationPhoto(request.getPhoto(), admissionId));
        registration.setStatus(RequestStatus.PENDING);
        pendingRegistrationRepository.save(registration);

        return new AuthDtos.RegistrationResponse("Registration request submitted. Please wait for admin approval.");
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String username = request.username().toLowerCase().trim();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isApproved()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your registration request is still pending admin approval");
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthDtos.AuthResponse(jwtService.generateToken(user.getUsername(), user.getRole().name()), Mapper.toUserSummary(user));
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
