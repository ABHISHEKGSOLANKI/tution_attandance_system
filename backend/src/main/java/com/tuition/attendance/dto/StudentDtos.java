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
            boolean fingerprintRegistered,
            double attendancePercentage
    ) { }

    public record AdminAnalyticsResponse(
            List<AuthDtos.DailyAttendancePoint> dailyAttendance,
            List<AuthDtos.MonthlyAttendancePoint> monthlyAttendance,
            List<StudentListItem> studentWiseAttendance
    ) { }

}
