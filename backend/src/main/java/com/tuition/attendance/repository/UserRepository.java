package com.tuition.attendance.repository;

import com.tuition.attendance.entities.User;
import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByAdmissionIdIgnoreCase(String admissionId);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByAdmissionIdIgnoreCase(String admissionId);
    List<User> findByRole(Role role);
    List<User> findByRoleAndStudentClass(Role role, StudentClass studentClass);
    List<User> findByRoleAndApproved(Role role, boolean approved);
}
