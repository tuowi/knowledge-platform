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
 * 用户端jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        String requestPath = request.getRequestURI();
        log.info("用户请求路径：{}", requestPath);

        // 1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 获取用户信息
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            String username = claims.get(JwtClaimsConstant.USERNAME).toString();
            Integer identity = claims.get(JwtClaimsConstant.USER_IDENTITY) != null
                    ? Integer.valueOf(claims.get(JwtClaimsConstant.USER_IDENTITY).toString())
                    : 0; // 默认为普通用户

            log.info("当前用户：id={}, username={}, identity={}", userId, username, identity);

            // 存储到ThreadLocal
            BaseContext.setCurrentUser(userId, identity);
            BaseContext.setCurrentUserToken(token);

            // ========== 权限校验：部分接口需要队长身份 ==========

            // 检查：团队管理相关接口需要队长身份
            if (requestPath.matches("/user/teams/\\d+/invitations.*") ||  // 发送邀请
                    requestPath.matches("/user/teams/\\d+/applications.*") ||  // 审核申请
                    requestPath.matches("/user/teams/\\d+/members/\\d+") ||    // 移出成员
                    requestPath.contains("/user/teams/") && request.getMethod().equals("DELETE")) {  // 解散团队

                if (identity != 1) {
                    log.warn("权限不足：非队长用户尝试操作团队管理接口，userId={}, identity={}", userId, identity);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅团队队长可操作\"}");
                    return false;
                }
            }

            // 检查：团队文档审核提交需要队长身份
            if (requestPath.contains("/user/documents/") && requestPath.contains("/submit-audit")) {
                if (identity != 1) {
                    log.warn("权限不足：非队长用户尝试提交文档审核，userId={}, identity={}", userId, identity);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅团队队长可提交文档审核\"}");
                    return false;
                }
            }

            // 检查：团队分类维护需要队长身份
            if (requestPath.contains("/user/teams/") &&
                    (requestPath.contains("/categories") || requestPath.contains("/tags")) &&
                    (request.getMethod().equals("POST") || request.getMethod().equals("PUT") || request.getMethod().equals("DELETE"))) {
                if (identity != 1) {
                    log.warn("权限不足：非队长用户尝试维护团队分类/标签，userId={}, identity={}", userId, identity);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅团队队长可维护团队分类和标签\"}");
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