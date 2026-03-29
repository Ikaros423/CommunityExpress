package com.express.system.dto.query;

import com.express.system.common.page.PageRequest;
import com.express.system.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户分页查询参数")
public class UserPageQuery extends PageRequest {

    @Schema(description = "用户名(手机号)")
    private String username;

    @Schema(description = "角色")
    private UserRole role;

    @Schema(description = "状态")
    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
