package com.school.service.impl;

import com.school.dto.OperationLogResponse;
import com.school.dto.PageResult;
import com.school.entity.SysOperationLog;
import com.school.repository.SysOperationLogRepository;
import com.school.security.LoginUser;
import com.school.service.OperationLogService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    /** 日志模块：认证 */
    public static final String MODULE_AUTH = "AUTH";
    /** 日志模块：用户管理 */
    public static final String MODULE_USER = "USER";

    private final SysOperationLogRepository repository;
    private final OperationLogWriter writer;

    public OperationLogServiceImpl(SysOperationLogRepository repository, OperationLogWriter writer) {
        this.repository = repository;
        this.writer = writer;
    }

    @Override
    public void logLogin(Long userId, String username, String realName, boolean success, String detail) {
        save(userId, username, realName, MODULE_AUTH, "LOGIN", detail, success);
    }

    @Override
    public void logCurrent(String module, String action, String detail, boolean success) {
        Long userId = null;
        String username = "anonymous";
        String realName = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            userId = loginUser.getUserId();
            username = loginUser.getUsername();
            realName = loginUser.getRealName();
        }

        save(userId, username, realName, module, action, detail, success);
    }

    @Override
    public void logSystem(String module, String action, String detail, boolean success) {
        save(null, "system", "系统", module, action, detail, success);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OperationLogResponse> page(int page, int size, String username, String module,
                                                 String action, Boolean success,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<SysOperationLog> pageData = repository.findAll(
                buildSpecification(username, module, action, success, startTime, endTime), pageable);

        List<OperationLogResponse> records = pageData.getContent().stream()
                .map(OperationLogResponse::from)
                .toList();

        return new PageResult<>(pageData.getTotalElements(), pageNo, pageSize, records);
    }

    /**
     * 写入一条日志：任何异常都只记录告警，绝不影响主业务流程
     */
    private void save(Long userId, String username, String realName,
                      String module, String action, String detail, boolean success) {
        try {
            SysOperationLog entity = new SysOperationLog();
            entity.setUserId(userId);
            entity.setUsername(StringUtils.hasText(username) ? username : "anonymous");
            entity.setRealName(realName);
            entity.setModule(module);
            entity.setAction(action);
            entity.setDetail(truncate(detail, 500));
            entity.setSuccess(success);
            entity.setCreatedAt(LocalDateTime.now());

            HttpServletRequest request = currentRequest();
            if (request != null) {
                entity.setIp(resolveIp(request));
                entity.setUserAgent(truncate(request.getHeader("User-Agent"), 255));
            }

            writer.save(entity);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }

    private Specification<SysOperationLog> buildSpecification(String username, String module,
                                                              String action, Boolean success,
                                                              LocalDateTime startTime,
                                                              LocalDateTime endTime) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(username)) {
                String keyword = "%" + username.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("realName")), keyword)));
            }
            if (StringUtils.hasText(module)) {
                predicates.add(cb.equal(root.get("module"), module.trim()));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action.trim()));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        String remote = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remote)) {
            return "127.0.0.1";
        }
        return remote;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
