package com.tuition.attendance.dto;

import com.tuition.attendance.model.StudentClass;

import java.util.List;

public class StudentDtos {

    public record StudentListItem(
            Long id,
            String name,
            String email,
            StudentClass studentClass,
            boolean approved,
            boolean biometricFaceRegistered,
            double attendancePercentage,
            String username
    ) { }

    public record AdminAnalyticsResponse(
            List<AttendanceDtos.DailyAttendancePoint> dailyAttendance,
            List<AttendanceDtos.MonthlyAttendancePoint> monthlyAttendance,
            List<StudentListItem> studentWiseAttendance
    ) { }

}
