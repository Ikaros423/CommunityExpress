package com.express.system.security;

import com.express.system.common.exception.BusinessException;
import com.express.system.entity.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CurrentUserProvider {

    public JwtUser getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser jwtUser)) {
            return null;
        }
        return jwtUser;
    }

    public JwtUser getCurrentUserOrThrow() {
        JwtUser jwtUser = getCurrentUserOrNull();
        if (jwtUser == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return jwtUser;
    }

    public JwtUser requireRole(UserRole... roles) {
        JwtUser jwtUser = getCurrentUserOrThrow();
        if (roles == null || roles.length == 0) {
            return jwtUser;
        }
        for (UserRole role : roles) {
            if (jwtUser.getRole() == role) {
                return jwtUser;
            }
        }
        throw BusinessException.forbidden("需要角色权限: " + Arrays.toString(roles));
    }
}
