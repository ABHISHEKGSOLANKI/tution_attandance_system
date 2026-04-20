package com.tuition.attendance.repository;

import com.tuition.attendance.model.FingerprintCredential;
import com.tuition.attendance.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FingerprintCredentialRepository extends JpaRepository<FingerprintCredential, Long> {

    Optional<FingerprintCredential> findByFingerprintIdAndActiveTrue(String fingerprintId);

    List<FingerprintCredential> findByUserAndActiveTrue(User user);
}
