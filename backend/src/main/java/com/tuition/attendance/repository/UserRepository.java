package com.tuition.attendance.repository;

import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import com.tuition.attendance.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndStudentClass(Role role, StudentClass studentClass);

    List<User> findByRoleAndApproved(Role role, boolean approved);
}
