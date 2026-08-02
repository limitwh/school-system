package com.school.repository;

import com.school.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    Optional<SysUser> findByLoginNo(String loginNo);

    /** 支持用 username 或 login_no（学号/职工号）登录 */
    Optional<SysUser> findByUsernameOrLoginNo(String username, String loginNo);

    boolean existsByUsername(String username);
}
