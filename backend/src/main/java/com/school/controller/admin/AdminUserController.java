package com.school.controller.admin;

import com.school.common.Result;
import com.school.dto.AdminUserResponse;
import com.school.dto.ResetPasswordByLoginNameRequest;
import com.school.dto.ResetPasswordRequest;
import com.school.dto.ResetPasswordResult;
import com.school.security.LoginUser;
import com.school.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员 - 用户管理接口（仅系统管理员 ADMIN 可访问）
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 查询所有锁定状态的用户
     */
    @GetMapping("/locked")
    public Result<List<AdminUserResponse>> listLockedUsers() {
        return Result.success(adminUserService.listLockedUsers());
    }

    /**
     * 重置锁定用户密码：状态置为初始(0)并分配初始密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestBody @Valid ResetPasswordRequest request) {
        adminUserService.resetPassword(id, request);
        return Result.success();
    }

    /**
     * 按登录名重置任意用户密码：自动分配默认密码，状态置为初始(0)
     * <p>
     * 登录名支持用户名 / 学号 / 职工号（即 sys_user.username 或 sys_user.login_no）。
     */
    @PostMapping("/reset-password")
    public Result<ResetPasswordResult> resetPasswordByLoginName(
            Authentication authentication,
            @RequestBody @Valid ResetPasswordByLoginNameRequest request) {
        LoginUser currentUser = (LoginUser) authentication.getPrincipal();
        return Result.success(adminUserService.resetPasswordByLoginName(
                currentUser.getUserId(), request.getLoginName()));
    }
}
