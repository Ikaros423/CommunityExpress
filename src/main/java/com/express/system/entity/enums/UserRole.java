package com.express.system.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户角色枚举")
public enum UserRole {
    @Schema(description = "系统管理员")
    ADMIN("ADMIN"),
    @Schema(description = "驿站工作人员")
    STAFF("STAFF"),
    @Schema(description = "普通用户")
    USER("USER");

    @EnumValue
    private final String code;

    UserRole(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
