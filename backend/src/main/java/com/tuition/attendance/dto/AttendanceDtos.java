package com.tuition.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public class AttendanceDtos {

    public record DailyAttendancePoint(LocalDate date, long presentCount) { }

    public record MonthlyAttendancePoint(String month, long presentCount) { }

    public record AdminAnalyticsResponse(List<DailyAttendancePoint> dailyAttendance, List<MonthlyAttendancePoint> monthlyAttendance, List<StudentListItem> studentWiseAttendance) { }

}
