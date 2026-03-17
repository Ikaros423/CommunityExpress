package com.express.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import com.express.system.entity.enums.UserRole;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Getter
@Setter
@TableName("sys_user")
@Schema(name = "SysUser", description = "用户信息表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "登录账号")
    @TableField("username")
    private String username;

    @Schema(description = "登录密码")
    @TableField("password")
    private String password;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "角色: ADMIN-管理员, STAFF-工作人员, USER-普通用户")
    @TableField("role")
    private UserRole role;

    @Schema(description = "账号状态: 0-禁用, 1-正常")
    @TableField("status")
    private Byte status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
