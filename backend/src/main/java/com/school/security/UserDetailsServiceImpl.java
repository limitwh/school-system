package com.school.security;

import com.school.entity.SysUser;
import com.school.repository.SysUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 从数据库加载用户（支持 username 或 login_no）
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserRepository userRepository;

    public UserDetailsServiceImpl(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsernameOrLoginNo(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));
        return new LoginUser(user);
    }
}
