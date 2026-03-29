package com.express.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.express.system.common.exception.BusinessException;
import com.express.system.entity.SysUser;
import com.express.system.entity.enums.SmsBizType;
import com.express.system.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);

    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final int EXPIRE_MINUTES = 10;

    private final SysUserMapper sysUserMapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SmsCodeRecord> codeStore = new ConcurrentHashMap<>();

    public SmsCodeService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public String requestCode(String phone, SmsBizType bizType) {
        String normalizedPhone = safeTrim(phone);
        if (normalizedPhone == null || normalizedPhone.isBlank()) {
            throw BusinessException.badRequest("手机号不能为空");
        }
        if (bizType == null) {
            throw BusinessException.badRequest("验证码业务类型不能为空");
        }

        validatePhoneByBizType(normalizedPhone, bizType);

        String code = generateCode();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);
        codeStore.put(buildKey(normalizedPhone, bizType), new SmsCodeRecord(code, expireAt, 0));
        log.info("短信验证码(bizType={}, phone={}): {}", bizType, normalizedPhone, code);
        return code;
    }

    public void verifyCode(String phone, SmsBizType bizType, String code) {
        String normalizedPhone = safeTrim(phone);
        String normalizedCode = safeTrim(code);
        if (normalizedPhone == null || normalizedPhone.isBlank()) {
            throw BusinessException.badRequest("手机号不能为空");
        }
        if (bizType == null) {
            throw BusinessException.badRequest("验证码业务类型不能为空");
        }
        if (normalizedCode == null || normalizedCode.isBlank()) {
            throw BusinessException.badRequest("验证码不能为空");
        }

        String key = buildKey(normalizedPhone, bizType);
        SmsCodeRecord record = codeStore.get(key);
        if (record == null) {
            throw BusinessException.badRequest("验证码不存在或已过期");
        }
        if (record.isExpired()) {
            codeStore.remove(key);
            throw BusinessException.badRequest("验证码已过期");
        }
        if (record.attempts >= MAX_ATTEMPTS) {
            codeStore.remove(key);
            throw BusinessException.badRequest("验证码尝试次数过多");
        }
        if (!record.code.equals(normalizedCode)) {
            record.attempts++;
            throw BusinessException.badRequest("验证码错误");
        }

        codeStore.remove(key);
    }

    private void validatePhoneByBizType(String phone, SmsBizType bizType) {
        SysUser user = sysUserMapper.selectOne(
                new QueryWrapper<SysUser>()
                        .eq("username", phone)
                        .eq("is_deleted", 0)
        );
        if (bizType == SmsBizType.REGISTER) {
            if (user != null) {
                throw BusinessException.badRequest("手机号已注册");
            }
            return;
        }
        if (bizType == SmsBizType.PASSWORD_RESET) {
            if (user == null) {
                throw BusinessException.badRequest("手机号不存在");
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw BusinessException.badRequest("账号已被禁用");
            }
        }
    }

    private String buildKey(String phone, SmsBizType bizType) {
        return bizType.name() + ":" + phone;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", code);
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private static class SmsCodeRecord {
        private final String code;
        private final LocalDateTime expireAt;
        private int attempts;

        private SmsCodeRecord(String code, LocalDateTime expireAt, int attempts) {
            this.code = code;
            this.expireAt = expireAt;
            this.attempts = attempts;
        }

        private boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAt);
        }
    }
}
