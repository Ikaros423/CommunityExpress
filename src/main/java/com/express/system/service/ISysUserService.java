package com.express.system.service;

import com.express.system.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
public interface ISysUserService extends IService<SysUser> {

    java.util.List<SysUser> listByFilter(String username,
                                         com.express.system.entity.enums.UserRole role,
                                         Integer status);

    SysUser getDetail(Long id);

    SysUser createUser(SysUser user);

    SysUser updateUser(SysUser user);

    boolean deleteUser(Long id);

    SysUser getByIdIncludeDeleted(Long id);

    SysUser registerUser(String username, String password, String nickname);

    SysUser login(String account, String password);

}
