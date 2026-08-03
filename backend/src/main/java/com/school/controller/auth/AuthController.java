package com.school.controller.auth;

import com.school.common.Result;
import com.school.dto.ChangePasswordRequest;
import com.school.dto.CurrentUserResponse;
import com.school.dto.LoginRequest;
import com.school.dto.LoginResponse;
import com.school.dto.SetPasswordRequest;
import com.school.security.LoginUser;
import com.school.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 当前登录用户信息（需携带 Token）
     */
    @GetMapping("/me")
    public Result<CurrentUserResponse> me(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = List.copyOf(loginUser.getRoles());
        CurrentUserResponse resp = new CurrentUserResponse(
                loginUser.getUserId(),
                loginUser.getUsername(),
                loginUser.getRealName(),
                loginUser.getUser().getLoginNo(),
                loginUser.getUser().getStatus(),
                roles);
        return Result.success(resp);
    }

    /**
     * 初始状态用户设置初始密码（设置成功后状态置为正常）
     */
    @PostMapping("/set-password")
    public Result<Void> setPassword(Authentication authentication,
                                    @RequestBody @Valid SetPasswordRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        authService.setPassword(loginUser, request);
        return Result.success();
    }

    /**
     * 修改密码（含密码过期后的强制修改；成功后状态置为正常）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(Authentication authentication,
                                       @RequestBody @Valid ChangePasswordRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        authService.changePassword(loginUser, request);
        return Result.success();
    }
}
