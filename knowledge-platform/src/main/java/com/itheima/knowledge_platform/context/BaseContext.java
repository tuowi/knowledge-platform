package com.itheima.knowledge_platform.context;

/**
 * 线程上下文工具类
 * 用于存储当前请求的用户信息
 */
public class BaseContext {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 用户端：存储用户身份
    private static final ThreadLocal<Integer> userIdentityThreadLocal = new ThreadLocal<>();

    // 用户端令牌，用于调用 AI Agent 服务
    private static final ThreadLocal<String> userTokenThreadLocal = new ThreadLocal<>();

    // 管理端：存储管理员类型和账号
    private static final ThreadLocal<Integer> adminTypeThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> adminAccountThreadLocal = new ThreadLocal<>();

    // ========== 通用方法 ==========

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
        userIdentityThreadLocal.remove();
        userTokenThreadLocal.remove();
        adminTypeThreadLocal.remove();
        adminAccountThreadLocal.remove();
    }

    // ========== 用户端专用方法 ==========

    public static void setCurrentUser(Long userId, Integer identity) {
        threadLocal.set(userId);
        userIdentityThreadLocal.set(identity);
    }

    public static Long getCurrentUserId() {
        return threadLocal.get();
    }

    public static Integer getCurrentUserIdentity() {
        return userIdentityThreadLocal.get();
    }

    public static void setCurrentUserToken(String token) {
        userTokenThreadLocal.set(token);
    }

    public static String getCurrentUserToken() {
        return userTokenThreadLocal.get();
    }

    public static boolean isTeamLeader() {
        Integer identity = userIdentityThreadLocal.get();
        return identity != null && identity == 1;
    }

    // ========== 管理端专用方法 ==========

    public static void setCurrentAdmin(Long adminId, String adminAccount, Integer adminType) {
        threadLocal.set(adminId);
        adminAccountThreadLocal.set(adminAccount);
        adminTypeThreadLocal.set(adminType);
    }

    public static Long getCurrentAdminId() {
        return threadLocal.get();
    }

    public static String getCurrentAdminAccount() {
        return adminAccountThreadLocal.get();
    }

    public static Integer getCurrentAdminType() {
        return adminTypeThreadLocal.get();
    }

    public static boolean isSuperAdmin() {
        Integer adminType = adminTypeThreadLocal.get();
        return adminType != null && adminType == 1;
    }
}