package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.AttendanceDtos;
import com.tuition.attendance.dto.StudentDtos;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.repository.AttendanceRecordRepository;
import com.tuition.attendance.repository.BiometricFaceRepository;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.service.biometric.FaceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final AttendanceService attendanceService;
    public DashboardService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public AttendanceDtos.AdminAnalyticsResponse getAnalytics() {
        return attendanceService.getAnalytics();
    }
}
