package com.school.security;

import com.school.entity.SysRole;
import com.school.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 当前登录用户（Spring Security UserDetails 实现）
 */
public class LoginUser implements UserDetails {

    private final SysUser user;
    private final Set<String> roles;

    public LoginUser(SysUser user) {
        this.user = user;
        this.roles = user.getRoles().stream()
                .map(SysRole::getCode)
                .collect(Collectors.toSet());
    }

    public SysUser getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getRealName() {
        return user.getRealName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 仅状态为「正常(1)」允许登录 */
    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
