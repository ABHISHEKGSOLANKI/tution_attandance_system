package com.tuition.attendance.repository;

import com.tuition.attendance.entities.AttendanceRequest;
import com.tuition.attendance.model.RequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRequestRepository extends JpaRepository<AttendanceRequest, Long> {
    List<AttendanceRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<AttendanceRequest> findByStatusOrderByCreatedAtAsc(RequestStatus status);
    boolean existsByStudentIdAndDateAndStatusIn(Long studentId, LocalDate date, List<RequestStatus> statuses);
    List<AttendanceRequest> findByStatusAndCreatedAtBefore(RequestStatus status, LocalDateTime createdAt);
}
