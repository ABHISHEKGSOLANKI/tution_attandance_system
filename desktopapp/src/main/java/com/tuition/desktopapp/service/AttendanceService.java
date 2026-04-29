package com.tuition.desktopapp.service;

import com.tuition.desktopapp.dto.ApiDtos;
import com.tuition.desktopapp.exception.ApiException;
import com.tuition.desktopapp.model.AttendanceRecord;
import com.tuition.desktopapp.model.Student;
import com.tuition.desktopapp.repository.AttendanceRepository;
import com.tuition.desktopapp.repository.StudentRepository;
import com.tuition.desktopapp.service.fingerprint.FingerprintCaptureResult;
import com.tuition.desktopapp.service.fingerprint.FingerprintMatchResult;
import com.tuition.desktopapp.service.fingerprint.FingerprintService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FingerprintService fingerprintService;
    private final EncryptionService encryptionService;

    public AttendanceService(StudentRepository studentRepository,
                             AttendanceRepository attendanceRepository,
                             FingerprintService fingerprintService,
                             EncryptionService encryptionService) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.fingerprintService = fingerprintService;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public ApiDtos.AttendanceTriggerResponse triggerAttendanceScan() {
        return triggerAttendanceScan(null);
    }

    @Transactional
    public ApiDtos.AttendanceTriggerResponse triggerAttendanceScan(String mockTemplateOverride) {
        if (!fingerprintService.supportsLocalMatching()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    fingerprintService.providerName() + " capture works, but offline local matching is not possible through RD service. Use the vendor SDK/DLL integration for attendance matching."
            );
        }
        FingerprintCaptureResult capture = mockTemplateOverride == null || mockTemplateOverride.isBlank()
                ? fingerprintService.captureFingerprint()
                : new FingerprintCaptureResult(mockTemplateOverride, "OVERRIDE", true);
        log.info("Fingerprint captured for attendance scan, quality={}, mock={}", capture.quality(), capture.mock());

        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            String decryptedTemplate = encryptionService.decrypt(student.getEncryptedFingerprintTemplate());
            FingerprintMatchResult matchResult = fingerprintService.matchFingerprint(capture.template(), decryptedTemplate);
            if (matchResult.matched()) {
                return markAttendance(student, matchResult.score());
            }
        }

        log.warn("Fingerprint match failed for current scan");
        return new ApiDtos.AttendanceTriggerResponse(false, "Fingerprint not recognized. Attendance rejected.", null, null, null, null);
    }

    private ApiDtos.AttendanceTriggerResponse markAttendance(Student student, int matchScore) {
        LocalDate today = LocalDate.now();
        attendanceRepository.findByStudentIdAndAttendanceDate(student.getId(), today).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Attendance already marked for student today");
        });

        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(student);
        record.setAttendanceDate(today);
        attendanceRepository.save(record);

        log.info("Attendance marked successfully for studentId={}, matchScore={}", student.getStudentId(), matchScore);
        return new ApiDtos.AttendanceTriggerResponse(
                true,
                "Attendance marked successfully",
                student.getStudentId(),
                student.getName(),
                record.getAttendanceDate(),
                record.getTimestamp()
        );
    }

    @Transactional(readOnly = true)
    public ApiDtos.DashboardSummary getTodayDashboardSummary() {
        LocalDate today = LocalDate.now();
        List<AttendanceRecord> records = attendanceRepository.findAllByAttendanceDateOrderByTimestampDesc(today);
        long totalPresent = attendanceRepository.countByAttendanceDate(today);
        long pendingSync = attendanceRepository.countByAttendanceDateAndSyncedFalse(today);
        String syncStatus = pendingSync == 0 ? "Synced" : "Pending";

        return new ApiDtos.DashboardSummary(
                totalPresent,
                pendingSync,
                syncStatus,
                records.stream().map(record -> new ApiDtos.DashboardAttendanceItem(
                        record.getStudent().getStudentId(),
                        record.getStudent().getName(),
                        record.getStudent().getStudentClass(),
                        record.getAttendanceDate(),
                        record.getTimestamp(),
                        record.isSynced()
                )).toList()
        );
    }
}
