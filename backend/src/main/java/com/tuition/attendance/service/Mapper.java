package com.tuition.attendance.service;

import com.tuition.attendance.dto.AttendanceDtos;
import com.tuition.attendance.dto.AttendanceRequestDTO;
import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.dto.PendingRegistrationDTO;
import com.tuition.attendance.entities.AttendanceRecord;
import com.tuition.attendance.entities.AttendanceRequest;
import com.tuition.attendance.entities.PendingRegistration;
import com.tuition.attendance.entities.User;

public final class Mapper {

    private Mapper() {
    }

    public static AuthDtos.UserSummary toUserSummary(User user) {
        return new AuthDtos.UserSummary(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStudentClass(),
                user.isApproved(),
                user.isPasswordChangeRequired(),
                user.getAdmissionId(),
                user.getPhotoUrl()
        );
    }

    public static AttendanceDtos.AttendanceView toAttendanceView(AttendanceRecord attendanceRecord) {
        return new AttendanceDtos.AttendanceView(
                attendanceRecord.getId(),
                attendanceRecord.getStudent().getId(),
                attendanceRecord.getStudent().getName(),
                attendanceRecord.getStudent().getStudentClass(),
                attendanceRecord.getAttendanceDate(),
                attendanceRecord.getAttendanceType().name(),
                attendanceRecord.getScannedAt()
        );
    }

    public static PendingRegistrationDTO toPendingRegistrationDto(PendingRegistration registration) {
        return new PendingRegistrationDTO(
                registration.getId(),
                registration.getFirstName(),
                registration.getMiddleName(),
                registration.getLastName(),
                fullName(registration.getFirstName(), registration.getMiddleName(), registration.getLastName()),
                registration.getMobile(),
                registration.getEmail(),
                registration.getAdmissionId(),
                registration.getStandard(),
                registration.getPhotoUrl(),
                registration.getStatus(),
                registration.getCreatedAt(),
                registration.getApprovedAt()
        );
    }

    public static AttendanceRequestDTO toAttendanceRequestDto(AttendanceRequest request) {
        AttendanceRequestDTO dto = new AttendanceRequestDTO();
        dto.setId(request.getId());
        dto.setStudentId(request.getStudent().getId());
        dto.setStudentName(request.getStudent().getName());
        dto.setDate(request.getDate());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setApprovedAt(request.getApprovedAt());
        return dto;
    }

    public static String fullName(String firstName, String middleName, String lastName) {
        String middle = middleName == null || middleName.isBlank() ? "" : middleName.trim() + " ";
        return (firstName.trim() + " " + middle + lastName.trim()).trim().replaceAll("\\s+", " ");
    }
}
