package com.tuition.attendance.dto;

import com.tuition.attendance.model.RequestStatus;
import com.tuition.attendance.model.StudentClass;
import java.time.LocalDateTime;

public record PendingRegistrationDTO(
        Long id,
        String firstName,
        String middleName,
        String lastName,
        String fullName,
        String mobile,
        String email,
        String admissionId,
        StudentClass standard,
        String photoUrl,
        RequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime approvedAt
) { }
