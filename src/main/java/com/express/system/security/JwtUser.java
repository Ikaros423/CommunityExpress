package com.express.system.security;

import com.express.system.entity.enums.UserRole;

public class JwtUser {
    private final Long userId;
    private final String username;
    private final UserRole role;

    public JwtUser(Long userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
