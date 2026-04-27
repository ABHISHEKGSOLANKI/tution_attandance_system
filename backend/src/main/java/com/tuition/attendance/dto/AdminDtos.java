package com.tuition.attendance.dto;

import com.tuition.attendance.model.StudentClass;

public class AdminDtos {

    public record PendingRegistrationItem(Long id, String name, String email, StudentClass studentClass) { }

    public record ApprovalResponse(String message) { }
}
