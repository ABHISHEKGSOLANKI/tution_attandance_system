package com.tuition.desktopapp.scheduler;

import com.tuition.desktopapp.service.RetentionService;
import com.tuition.desktopapp.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceScheduler.class);
    private final SyncService syncService;
    private final RetentionService retentionService;

    public AttendanceScheduler(SyncService syncService, RetentionService retentionService) {
        this.syncService = syncService;
        this.retentionService = retentionService;
    }

    @Scheduled(cron = "${middleware.schedule.sync-cron}", zone = "${middleware.schedule.zone}")
    public void runDailySync() {
        log.info("Running scheduled attendance sync job");
        syncService.syncUnsyncedAttendance();
    }

    @Scheduled(cron = "${middleware.schedule.cleanup-cron}", zone = "${middleware.schedule.zone}")
    public void runDailyCleanup() {
        log.info("Running scheduled attendance cleanup job");
        retentionService.cleanupAttendanceOlderThanDays(30);
    }
}
