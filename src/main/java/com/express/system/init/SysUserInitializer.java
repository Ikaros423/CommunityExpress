package com.express.system.init;

import com.express.system.entity.SysUser;
import com.express.system.entity.enums.UserRole;
import com.express.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SysUserInitializer implements CommandLineRunner {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public void run(String... args) {
        long adminCount = sysUserService.lambdaQuery()
                .eq(SysUser::getRole, UserRole.ADMIN)
                .count();
        if (adminCount > 0) {
            return;
        }
        String adminUsername = "13900000001";
        long usernameCount = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, adminUsername)
                .count();
        if (usernameCount > 0) {
            return;
        }

        SysUser admin = new SysUser();
        admin.setUsername(adminUsername);
        admin.setPassword("123456");
        admin.setNickname("系统管理员");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus((byte) 1);
        sysUserService.createUser(admin);
    }
}
