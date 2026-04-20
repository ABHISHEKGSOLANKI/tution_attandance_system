package com.tuition.desktopapp.repository;

import com.tuition.desktopapp.model.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentId(String studentId);

    Optional<Student> findByStudentId(String studentId);
}
