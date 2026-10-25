package com.school.repository;

import com.school.entity.SysUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /** 按状态查询用户（如锁定状态 9） */
    List<SysUser> findByStatus(Integer status);

    /** 搜索可发消息的活跃用户（排除自己；keyword 为空时返回前若干人） */
    @Query("select u from SysUser u where u.id <> :userId and u.status = 1 " +
            "and (:keyword is null " +
            "     or lower(u.username) like lower(concat('%', cast(:keyword as string), '%')) " +
            "     or lower(u.realName) like lower(concat('%', cast(:keyword as string), '%'))) " +
            "order by u.realName")
    List<SysUser> searchActiveUsers(@Param("userId") Long userId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);
}
