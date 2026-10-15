package com.itheima.knowledge_platform.interceptor;

import com.itheima.knowledge_platform.constant.JwtClaimsConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.properties.JwtProperties;
import com.itheima.knowledge_platform.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器（管理端）
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        String requestPath = request.getRequestURI();
        log.info("管理员请求路径：{}", requestPath);

        // 1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);

            // 获取管理员信息
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            String adminAccount = claims.get(JwtClaimsConstant.ADMIN_ACCOUNT).toString();
            Integer adminType = claims.get(JwtClaimsConstant.ADMIN_TYPE) != null
                    ? Integer.valueOf(claims.get(JwtClaimsConstant.ADMIN_TYPE).toString())
                    : 0; // 默认为子管理员

            log.info("当前管理员：id={}, account={}, type={}", adminId, adminAccount, adminType);

            // 存储到ThreadLocal
            BaseContext.setCurrentAdmin(adminId, adminAccount, adminType);

            // ========== 权限校验：仅超级管理员可访问特定接口 ==========

            // 检查权限：子管理员管理接口仅限超级管理员访问
            if (requestPath.contains("/admin/admins")) {
                if (adminType != 1) {
                    log.warn("权限不足：普通管理员尝试访问子管理员管理接口，adminId={}, adminType={}", adminId, adminType);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅超级管理员可访问此接口\"}");
                    return false;
                }
            }

            // 检查权限：系统配置接口仅限超级管理员访问
            if (requestPath.contains("/admin/config")) {
                if (adminType != 1) {
                    log.warn("权限不足：普通管理员尝试访问系统配置接口，adminId={}, adminType={}", adminId, adminType);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅超级管理员可访问此接口\"}");
                    return false;
                }
            }

            // 检查权限：系统日志接口仅限超级管理员访问
            if (requestPath.contains("/admin/logs")) {
                if (adminType != 1) {
                    log.warn("权限不足：普通管理员尝试访问系统日志接口，adminId={}, adminType={}", adminId, adminType);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅超级管理员可访问此接口\"}");
                    return false;
                }
            }

            // 可选：超级管理员独有的其他接口
            // 例如：强制解散团队、全局系统任务等
            if (requestPath.contains("/admin/teams/force") ||
                    requestPath.contains("/admin/tasks/trigger")) {
                if (adminType != 1) {
                    log.warn("权限不足：普通管理员尝试访问超管专属接口，adminId={}, adminType={}", adminId, adminType);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅超级管理员可访问此接口\"}");
                    return false;
                }
            }

            // 3、通过，放行
            return true;

        } catch (Exception ex) {
            log.error("JWT校验失败：{}", ex.getMessage());
            // 4、不通过，响应401状态码
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已失效\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理ThreadLocal，防止内存泄漏
        BaseContext.removeCurrentId();
    }
}