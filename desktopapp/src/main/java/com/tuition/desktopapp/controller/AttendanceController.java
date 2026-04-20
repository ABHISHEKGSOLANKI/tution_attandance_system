package com.tuition.desktopapp.controller;

import com.tuition.desktopapp.dto.ApiDtos;
import com.tuition.desktopapp.service.AttendanceService;
import com.tuition.desktopapp.service.SyncService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SyncService syncService;

    public AttendanceController(AttendanceService attendanceService, SyncService syncService) {
        this.attendanceService = attendanceService;
        this.syncService = syncService;
    }

    @PostMapping("/scan")
    public ApiDtos.AttendanceTriggerResponse triggerScan(@RequestBody(required = false) ApiDtos.AttendanceTriggerRequest request) {
        String templateOverride = request == null ? null : request.mockTemplate();
        return attendanceService.triggerAttendanceScan(templateOverride);
    }

    @PostMapping("/sync")
    public ApiDtos.SyncResult syncNow() {
        return syncService.syncUnsyncedAttendance();
    }
}
