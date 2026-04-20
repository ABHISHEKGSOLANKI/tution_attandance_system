package com.tuition.attendance.service;

import com.tuition.attendance.dto.AuthDtos;
import com.tuition.attendance.model.AttendanceRecord;
import com.tuition.attendance.model.User;

public final class Mapper {

    private Mapper() {
    }

    public static AuthDtos.UserSummary toUserSummary(User user) {
        return new AuthDtos.UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStudentClass(),
                user.isApproved(),
                user.isPasswordChangeRequired()
        );
    }

    public static AuthDtos.AttendanceView toAttendanceView(AttendanceRecord attendanceRecord) {
        return new AuthDtos.AttendanceView(
                attendanceRecord.getId(),
                attendanceRecord.getStudent().getId(),
                attendanceRecord.getStudent().getName(),
                attendanceRecord.getStudent().getStudentClass(),
                attendanceRecord.getAttendanceDate(),
                attendanceRecord.getFingerprintId(),
                attendanceRecord.getScannedAt()
        );
    }
}
