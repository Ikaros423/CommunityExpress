package com.express.system.service;

import com.express.system.entity.SysUser;
import com.express.system.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final int EXPIRE_MINUTES = 10;

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, ResetCode> codeStore = new ConcurrentHashMap<>();

    public PasswordResetService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public String requestCode(String phone) {
        SysUser user = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                        .eq("username", phone)
                        .eq("is_deleted", 0)
        );
        if (user == null) {
            throw new RuntimeException("手机号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        String code = generateCode();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);
        codeStore.put(phone, new ResetCode(code, expireAt, 0));
        return code;
    }

    public void confirmReset(String phone, String code, String newPassword) {
        ResetCode resetCode = codeStore.get(phone);
        if (resetCode == null) {
            throw new RuntimeException("验证码不存在或已过期");
        }
        if (resetCode.isExpired()) {
            codeStore.remove(phone);
            throw new RuntimeException("验证码已过期");
        }
        if (resetCode.attempts >= MAX_ATTEMPTS) {
            codeStore.remove(phone);
            throw new RuntimeException("验证码尝试次数过多");
        }
        if (!resetCode.code.equals(code)) {
            resetCode.attempts++;
            throw new RuntimeException("验证码错误");
        }

        SysUser user = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                        .eq("username", phone)
                        .eq("is_deleted", 0)
        );
        if (user == null) {
            throw new RuntimeException("手机号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(update);
        codeStore.remove(phone);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", code);
    }

    private static class ResetCode {
        private final String code;
        private final LocalDateTime expireAt;
        private int attempts;

        private ResetCode(String code, LocalDateTime expireAt, int attempts) {
            this.code = code;
            this.expireAt = expireAt;
            this.attempts = attempts;
        }

        private boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAt);
        }
    }
}
