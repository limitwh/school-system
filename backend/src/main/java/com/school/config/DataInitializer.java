package com.school.config;

import com.school.entity.SysRole;
import com.school.entity.SysUser;
import com.school.repository.SysRoleRepository;
import com.school.repository.SysUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时初始化：角色种子数据 + 初始管理员账号
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /** 角色定义：[code, name, dataScope, description] */
    private static final String[][] ROLES = {
            {"ADMIN", "系统管理员", "ALL", "最高权限，可读写所有信息"},
            {"PRINCIPAL", "校长", "ALL_STAFF_STUDENT", "读写所有教师、年级管理员和学生信息"},
            {"GRADE_ADMIN", "年级管理员", "GRADE", "只能读写本年级的信息"},
            {"TEACHER", "教师", "CLASS", "只能读写本人任教班级学生的信息"},
            {"STUDENT", "学生", "SELF", "只能查看自己的信息"},
    };

    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysRoleRepository roleRepository,
                           SysUserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initAdmin();
    }

    private void initRoles() {
        for (String[] role : ROLES) {
            roleRepository.findByCode(role[0]).orElseGet(() -> {
                SysRole r = new SysRole();
                r.setCode(role[0]);
                r.setName(role[1]);
                r.setDataScope(role[2]);
                r.setDescription(role[3]);
                return roleRepository.save(r);
            });
        }
    }

    private void initAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        SysRole adminRole = roleRepository.findByCode("ADMIN").orElseThrow();

        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setLoginNo("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("系统管理员");
        admin.setStatus(1);
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
    }
}
