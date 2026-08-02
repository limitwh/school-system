package com.school.controller.auth;

import com.school.common.Result;
import com.school.dto.CurrentUserResponse;
import com.school.dto.LoginRequest;
import com.school.dto.LoginResponse;
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
                roles);
        return Result.success(resp);
    }
}
