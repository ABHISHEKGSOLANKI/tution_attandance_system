package com.tuition.attendance.service;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.entities.FingerprintCredential;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.FingerprintCredentialRepository;
import com.tuition.attendance.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FingerprintService {

    private final FingerprintCredentialRepository fingerprintRepository;
    private final UserRepository userRepository;

    public FingerprintService(FingerprintCredentialRepository fingerprintRepository, UserRepository userRepository) {
        this.fingerprintRepository = fingerprintRepository;
        this.userRepository = userRepository;
    }

    public void registerFingerprint(Long studentId, AuthDtos.FingerprintRegistrationRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fingerprint registration is only allowed for students");
        }
        if (!student.isApproved()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Approve the student registration before linking a fingerprint");
        }
        fingerprintRepository.findByFingerprintIdAndActiveTrue(request.fingerprintId()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Fingerprint ID already registered");
        });

        FingerprintCredential credential = new FingerprintCredential();
        credential.setUser(student);
        credential.setFingerprintId(request.fingerprintId());
        credential.setProvider(request.provider() == null || request.provider().isBlank() ? "SIMULATED_SCANNER" : request.provider());
        fingerprintRepository.save(credential);
    }

    public User resolveStudent(String fingerprintId) {
        User user = fingerprintRepository.findByFingerprintIdAndActiveTrue(fingerprintId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Fingerprint did not match any registered student"))
                .getUser();
        if (!user.isApproved()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Student is not approved for attendance yet");
        }
        return user;
    }

    public boolean hasFingerprint(User user) {
        return !fingerprintRepository.findByUserAndActiveTrue(user).isEmpty();
    }
}
