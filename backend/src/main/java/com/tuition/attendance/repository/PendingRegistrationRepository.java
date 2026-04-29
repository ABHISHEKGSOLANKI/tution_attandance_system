package com.tuition.attendance.repository;

import com.tuition.attendance.entities.PendingRegistration;
import com.tuition.attendance.model.RequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    List<PendingRegistration> findByStatusOrderByCreatedAtAsc(RequestStatus status);
    Optional<PendingRegistration> findByEmailIgnoreCase(String email);
    Optional<PendingRegistration> findByAdmissionIdIgnoreCase(String admissionId);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByAdmissionIdIgnoreCase(String admissionId);
}
