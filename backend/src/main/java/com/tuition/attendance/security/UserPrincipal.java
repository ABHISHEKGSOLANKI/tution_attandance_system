package com.tuition.attendance.security;

import com.tuition.attendance.model.Role;
import com.tuition.attendance.model.StudentClass;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final StudentClass studentClass;
    private final boolean active;

    public UserPrincipal(Long id, String name, String email, String passwordHash, Role role, StudentClass studentClass, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.studentClass = studentClass;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getDisplayName() { return name; }
    public Role getRole() { return role; }
    public StudentClass getStudentClass() { return studentClass; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_" + role.name())); }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return active; }
    @Override public boolean isAccountNonLocked() { return active; }
    @Override public boolean isCredentialsNonExpired() { return active; }
    @Override public boolean isEnabled() { return active; }
}
