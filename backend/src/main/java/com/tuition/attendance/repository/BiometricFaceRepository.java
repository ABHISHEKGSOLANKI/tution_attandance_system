package com.tuition.attendance.repository;

import com.tuition.attendance.entities.BiometricFace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricFaceRepository extends JpaRepository<BiometricFace, Integer> {
    List<BiometricFace> findByStudentId(Long id);
}
