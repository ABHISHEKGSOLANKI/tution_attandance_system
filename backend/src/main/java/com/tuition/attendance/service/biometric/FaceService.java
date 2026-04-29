package com.tuition.attendance.service.biometric;

import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.BiometricFaceRepository;
import org.springframework.stereotype.Service;

@Service
public class FaceService {

    private final BiometricFaceRepository biometricFaceRepository;

    public FaceService(BiometricFaceRepository biometricFaceRepository) {
        this.biometricFaceRepository = biometricFaceRepository;
    }

    public boolean hasFaceRegistered(User student) {
        return (student.getPhotoUrl() != null && !student.getPhotoUrl().isBlank())
                || !biometricFaceRepository.findByStudentId(student.getId()).isEmpty();
    }
}
