package com.tuition.desktopapp.service;

import com.tuition.desktopapp.dto.ApiDtos;
import com.tuition.desktopapp.exception.ApiException;
import com.tuition.desktopapp.model.Student;
import com.tuition.desktopapp.repository.StudentRepository;
import com.tuition.desktopapp.service.fingerprint.FingerprintCaptureResult;
import com.tuition.desktopapp.service.fingerprint.FingerprintService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository studentRepository;
    private final FingerprintService fingerprintService;
    private final EncryptionService encryptionService;

    public StudentService(StudentRepository studentRepository,
                          FingerprintService fingerprintService,
                          EncryptionService encryptionService) {
        this.studentRepository = studentRepository;
        this.fingerprintService = fingerprintService;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public ApiDtos.StudentRegistrationResponse registerStudent(ApiDtos.StudentRegistrationRequest request) {
        if (studentRepository.existsByStudentId(request.studentId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Student ID already registered");
        }

        FingerprintCaptureResult captureResult = fingerprintService.captureFingerprint();
        log.info("Fingerprint capture completed for registration studentId={}, quality={}, mock={}",
                request.studentId(), captureResult.quality(), captureResult.mock());
        String templateToStore = captureResult.mock() ? "MOCK:" + request.studentId() : captureResult.template();

        Student student = new Student();
        student.setStudentId(request.studentId());
        student.setName(request.name());
        student.setStudentClass(request.studentClass());
        student.setEncryptedFingerprintTemplate(encryptionService.encrypt(templateToStore));
        studentRepository.save(student);

        return new ApiDtos.StudentRegistrationResponse(
                student.getId(),
                student.getStudentId(),
                student.getName(),
                student.getStudentClass(),
                student.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.StudentListItem> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> new ApiDtos.StudentListItem(
                        student.getId(),
                        student.getStudentId(),
                        student.getName(),
                        student.getStudentClass(),
                        student.getCreatedAt()
                ))
                .toList();
    }
}
