package com.school.controller.admin;

import com.school.common.Result;
import com.school.dto.AdminUserResponse;
import com.school.dto.ResetPasswordRequest;
import com.school.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
