package com.tuition.desktopapp.service;

import com.tuition.desktopapp.config.AppProperties;
import com.tuition.desktopapp.dto.ApiDtos;
import com.tuition.desktopapp.exception.ApiException;
import com.tuition.desktopapp.model.AttendanceRecord;
import com.tuition.desktopapp.repository.AttendanceRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);
    private final AttendanceRepository attendanceRepository;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;

    public SyncService(AttendanceRepository attendanceRepository,
                       AppProperties appProperties,
                       RestTemplate restTemplate) {
        this.attendanceRepository = attendanceRepository;
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public ApiDtos.SyncResult syncUnsyncedAttendance() {
        List<AttendanceRecord> unsyncedRecords = attendanceRepository.findAllBySyncedFalseOrderByTimestampAsc();
        if (unsyncedRecords.isEmpty()) {
            log.info("No unsynced attendance records available for sync");
            return new ApiDtos.SyncResult(0, 0, 0, "No unsynced attendance records found");
        }

        String url = buildSyncUrl();
        ApiDtos.AttendanceBulkSyncRequest payload = new ApiDtos.AttendanceBulkSyncRequest(
                unsyncedRecords.stream()
                        .map(record -> new ApiDtos.AttendanceBulkItem(
                                record.getId(),
                                record.getStudent().getStudentId(),
                                record.getStudent().getName(),
                                record.getStudent().getStudentClass(),
                                record.getAttendanceDate(),
                                record.getTimestamp()
                        ))
                        .toList()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (appProperties.getBackend().getApiKey() != null && !appProperties.getBackend().getApiKey().isBlank()) {
            headers.set("X-API-KEY", appProperties.getBackend().getApiKey());
        }

        try {
            log.info("Syncing {} attendance records to backend {}", unsyncedRecords.size(), url);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Attendance sync failed with status {}", response.getStatusCode());
                return new ApiDtos.SyncResult(unsyncedRecords.size(), 0, unsyncedRecords.size(), "Sync failed with non-success status");
            }

            unsyncedRecords.forEach(record -> record.setSynced(true));
            attendanceRepository.saveAll(unsyncedRecords);
            log.info("Attendance sync completed successfully. Synced {} records", unsyncedRecords.size());
            return new ApiDtos.SyncResult(unsyncedRecords.size(), unsyncedRecords.size(), 0, "Sync completed successfully");
        } catch (RestClientException ex) {
            log.error("Attendance sync failed due to backend/API error", ex);
            return new ApiDtos.SyncResult(unsyncedRecords.size(), 0, unsyncedRecords.size(), "Sync failed. Will retry in next scheduled run");
        }
    }

    private String buildSyncUrl() {
        String baseUrl = appProperties.getBackend().getBaseUrl();
        String path = appProperties.getBackend().getAttendanceBulkPath();
        if (baseUrl == null || baseUrl.isBlank() || path == null || path.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Backend sync URL is not configured");
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
