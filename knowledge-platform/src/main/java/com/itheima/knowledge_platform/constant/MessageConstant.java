package com.itheima.knowledge_platform.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String ALREADY_EXISTS = "已存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String TOKEN_ERROR = "令牌格式错误";
    public static final String PASSWORD_EDIT_FAILED = "新旧密码不可一致";
    public static final String OLD_PASSWORD_ERROR = "旧密码错误";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String DOCUMENT_NOT_FOUND = "文档不存在";
    public static final String PATH_NOT_FOUND = "文档路径所指文件不存在";
    public static final String TEMP_DOCUMENT_NOT_FOUND = "临时文件不存在";
    public static final String DOCUMENT_LOCKED = "文档不允许被修改";
    public static final String DOCUMENT_EDIT_FAILED = "文档已被他人修改，请刷新后重试";
    public static final String DOCUMENT_VERSION_ILLEGAL = "文档版本号不合法";
    public static final String TEAMID_NOT_ALLOWED_NULL = "想看团队文档，团队id不可为空";
    public static final String DOCUMENT_NOT_ALLOWED_FAVORITE = "文档不允许被收藏";
    public static final String FAVORITE_NOT_FOUND = "未收藏该文档";
    public static final String CATEGORY_NAME_NOT_ALLOWED_NONE = "分类姓名不可为空";
    public static final String CATEGORY_NAME_EXIST = "分类名称已经存在，不可重复";
    public static final String PARENT_NOT_FOUND = "父分类不存在";
    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String USER_NO_PERMISSION_TO_MODIFY = "无权修改此分类";
    public static final String CATEGORY_EXIST_CHILDREN = "该分类下存在子分类，请先删除子分类";
    public static final String CATEGORY_EXIST_DOCUMENT = "请先将分类下的文档移动到其他分类或删除文档";
    public static final String QUERY_LACK = "请求体条件缺漏";
    public static final String TAG_NAME_EXIST = "标签名称已存在，不可重复";
    public static final String TAG_NOT_FOUND = "标签未找到";
    public static final String PARENT_COMMENT_NOT_FOUND = "父评论不存在";
    public static final String COMMENT_CONTENT_NOT_NULL = "评论内容不可为空";
    public static final String COMMENT_NOT_FOUND = "评论未找到";
    public static final String NO_PERMISSION_DELETE_COMMENT = "无权删除评论";
    public static final String OWNER_TEAM_FULL = "该成员名下团队已满";
    public static final String TEAM_NAME_EXIST = "队伍名称已存在，不可重复";
    public static final String USER_NOT_IN_TEAM = "该用户不在队伍中，无权查看该团队详细";
    public static final String USER_IS_NOT_LEADER = "该用户不是队长，无权限查看";
    public static final String TEAM_NOT_FOUND = "该团队已解散";
    public static final String USER_IS_IN_TEAM = "该用户已经是该团队成员";
    public static final String TEAM_MEMBER_OVER = "该团队人数已达上限，不可再邀请";
    public static final String ALREADY_INVITE = "用户在该团队已经存在未处理邀请";
    public static final String INVITE_NOT_FOUND = "该邀请不存在";
    public static final String NO_PERMISSION_TO_HANDLE_INVITATION = "当前登录用户无权限处理此邀请";
    public static final String INVITATION_HAS_BEEN_PROCESSED = "邀请已处理过";
    public static final String INVITATION_EXPIRED = "邀请已过期";
    public static final String TEAM_IS_FULL = "团队人数已满";
    public static final String TEAM_NOT_EXIST = "该团队不存在";
    public static final String USER_NOT_LEADER = "该用户是队长，无需申请入队";
    public static final String NO_PERMISSION_TO_DELETE_MYSELF = "不能移出自己，如需退出请使用退出团队接口";
    public static final String NO_PERMISSION_TO_DELETE_LEADER = "队长不能直接退出团队，请先转让队长身份或解散团队";
    public static final String USER_HAS_DOCUMENT = "该用户拥有团队文档，请先转移或删除其文档后再移出";
    public static final String TEAM_HAS_DOCUMENT = "该团队拥有团队文档，请先转移或删除其文档后再移出";
    public static final String NO_PERMISSION_TO_DELETE_CATEGORY = "该分类下包含异常文档，不可删除";
    public static final String NO_PERMISSION_TO_DELETE_TAG = "该标签下包含异常文档，不可删除";
    public static final String REJECT_REASON_NOT_NULL = "驳回时驳回理由不可为空";
    public static final String AUDIT_STATUS_ERROR = "审核状态有误";
    public static final String DOCUMENT_STATUS_ERROR = "文档状态有误";
    public static final String DELETE_COMMENT_COUNT_OVER = "批量删除评论数目超过上限";
    public static final String DELETE_COMMENT_NOT_NULL = "批量删除评论不可为空";
    public static final String NO_PERMISSION_TO_SET_STATUS_MYSELF = "不可禁用自己";
    public static final String ADMIN_NO_PERMISSION_TO_MODIFY = "该管理员无权修改";
    public static final String ADMIN_HAS_BEEN_EXIST = "此管理员账号已经存在，无法添加";
    public static final String NO_PERMISSION_TO_READ_DOCUMENT = "无权限读取文档";
    public static final String NO_PERMISSION_TO_GET = "无权限访问";
}
