package com.express.system.service;

import com.express.system.common.exception.BusinessException;
import com.express.system.entity.SysUser;
import com.express.system.entity.enums.SmsBizType;
import com.express.system.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SmsCodeService smsCodeService;

    public PasswordResetService(SysUserMapper sysUserMapper,
                                PasswordEncoder passwordEncoder,
                                SmsCodeService smsCodeService) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.smsCodeService = smsCodeService;
    }

    public String requestCode(String phone) {
        return smsCodeService.requestCode(phone, SmsBizType.PASSWORD_RESET);
    }

    public void confirmReset(String phone, String code, String newPassword) {
        smsCodeService.verifyCode(phone, SmsBizType.PASSWORD_RESET, code);

        SysUser user = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                        .eq("username", phone)
                        .eq("is_deleted", 0)
        );
        if (user == null) {
            throw BusinessException.badRequest("手机号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw BusinessException.badRequest("账号已被禁用");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(update);
    }
}
