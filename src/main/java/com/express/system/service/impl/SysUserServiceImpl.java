package com.express.system.service.impl;

import com.express.system.entity.SysUser;
import com.express.system.entity.enums.UserRole;
import com.express.system.mapper.SysUserMapper;
import com.express.system.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<SysUser> listByFilter(String username, UserRole role, Integer status) {
        String normalizedUsername = safeTrim(username);
        // 按用户名/角色/状态过滤，并对返回结果做密码脱敏。
        List<SysUser> list = this.lambdaQuery()
                .like(normalizedUsername != null && !normalizedUsername.isBlank(), SysUser::getUsername, normalizedUsername)
                .eq(role != null, SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getUpdateTime)
                .orderByDesc(SysUser::getCreateTime)
                .list();
        for (SysUser user : list) {
            user.setPassword(null);
        }
        return list;
    }

    @Override
    public SysUser getDetail(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        SysUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser createUser(SysUser user) {
        if (user == null) {
            throw new RuntimeException("用户信息不能为空");
        }
        String username = safeTrim(user.getUsername());
        String password = safeTrim(user.getPassword());
        if (username == null || username.isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        if (user.getRole() == null) {
            throw new RuntimeException("角色不能为空");
        }

        checkUsernameUnique(username, null);
        // 持久化时使用 BCrypt 加密密码。
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(safeTrim(user.getNickname()));
        user.setEmail(safeTrim(user.getEmail()));

        if (user.getStatus() == null) {
            user.setStatus((byte) 1);
        }
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        boolean saved = this.save(user);
        if (!saved) {
            throw new RuntimeException("新增用户失败");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser updateUser(SysUser user) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        SysUser existing = this.getById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        // 仅更新传入字段，避免空值覆盖。
        SysUser updateEntity = new SysUser();
        updateEntity.setId(user.getId());

        String username = safeTrim(user.getUsername());
        if (username != null && !username.isBlank() && !username.equals(existing.getUsername())) {
            checkUsernameUnique(username, user.getId());
            updateEntity.setUsername(username);
        }

        String password = safeTrim(user.getPassword());
        if (password != null && password.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        if (password != null && !password.isBlank()) {
            updateEntity.setPassword(passwordEncoder.encode(password));
        }

        String nickname = safeTrim(user.getNickname());
        if (nickname != null) {
            updateEntity.setNickname(nickname);
        }
        String email = safeTrim(user.getEmail());
        if (email != null) {
            updateEntity.setEmail(email);
        }
        if (user.getRole() != null) {
            updateEntity.setRole(user.getRole());
        }
        if (user.getStatus() != null) {
            updateEntity.setStatus(user.getStatus());
        }

        updateEntity.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(updateEntity);
        if (!updated) {
            throw new RuntimeException("更新用户失败");
        }
        SysUser updatedUser = this.getById(user.getId());
        if (updatedUser != null) {
            updatedUser.setPassword(null);
        }
        return updatedUser;
    }

    @Override
    public boolean deleteUser(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        SysUser existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new RuntimeException("删除用户失败");
        }
        return true;
    }

    @Override
    public SysUser registerUser(String username, String password, String nickname) {
        String normalizedUsername = safeTrim(username);
        String normalizedPassword = safeTrim(password);
        if (normalizedUsername == null || normalizedUsername.isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (normalizedPassword == null || normalizedPassword.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        checkUsernameUnique(normalizedUsername, null);

        // 注册时固定 USER 角色并加密密码。
        SysUser user = new SysUser();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(normalizedPassword));
        user.setNickname(safeTrim(nickname));
        user.setRole(UserRole.USER);
        user.setStatus((byte) 1);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        boolean saved = this.save(user);
        if (!saved) {
            throw new RuntimeException("注册失败");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser login(String account, String password) {
        String normalizedAccount = safeTrim(account);
        String normalizedPassword = safeTrim(password);
        if (normalizedAccount == null || normalizedAccount.isBlank()) {
            throw new RuntimeException("账号不能为空");
        }
        if (normalizedPassword == null || normalizedPassword.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }

        // 通过用户名（手机号）登录。
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getUsername, normalizedAccount)
                .one();
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!passwordEncoder.matches(normalizedPassword, user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        user.setPassword(null);
        return user;
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        long count = this.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count();
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}
