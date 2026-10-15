create table category
(
    id         bigint auto_increment comment '主键'
        primary key,
    parent_id  bigint                             null comment '父分类ID，顶级为null',
    name       varchar(64)                        not null comment '分类名称',
    team_id    bigint                             null comment '所属团队ID（逻辑外键）',
    sort_order int      default 0                 not null comment '同级排序序号',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '分类表';

create index idx_parent_id
    on category (parent_id);

create index idx_team_id
    on category (team_id);

create table chat_session
(
    session_id   varchar(64)                            not null comment '唯一会话主键'
        primary key,
    session_name varchar(128) default '新对话'          not null comment '会话名称',
    json_path    varchar(256)                           null comment '对话记录JSON文件存储路径',
    created_at   datetime     default CURRENT_TIMESTAMP not null comment '会话创建时间',
    updated_at   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间（用于倒序排列）'
)
    comment 'AI会话主表';

create table comment
(
    id          bigint auto_increment comment '主键'
        primary key,
    document_id bigint                             not null comment '关联文档ID（逻辑外键）',
    parent_id   bigint                             null comment '父评论ID（回复用）',
    user_id     bigint                             not null comment '评论人ID（逻辑外键）',
    content     text                               not null comment '评论内容',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '评论表';

create index idx_document_id
    on comment (document_id);

create index idx_parent_id
    on comment (parent_id);

create index idx_user_id
    on comment (user_id);

create table document
(
    id             bigint auto_increment comment '主键'
        primary key,
    title          varchar(256)                         not null comment '文档标题',
    content        text                                 null comment '文档地址',
    version        int        default 0                 not null comment '乐观锁版本号',
    status         tinyint    default 0                 not null comment '文档基础状态：0=草稿，1=已发布，2=已归档',
    audit_status   tinyint(5) default 0                 not null comment '审核状态：0=编辑中，1=待审核，2=已通过，3=已驳回',
    visibility     tinyint    default 0                 not null comment '可见范围：0=私有，1=团队内，2=公开',
    author_id      bigint                               not null comment '作者ID（逻辑外键）',
    category_id    bigint                               null comment '所属分类ID（逻辑外键）',
    team_id        bigint                               null comment '所属团队ID（逻辑外键）',
    view_count     int        default 0                 not null comment '浏览量',
    like_count     int        default 0                 not null comment '点赞数',
    favorite_count int        default 0                 not null comment '收藏数量',
    reject_reason  varchar(256)                         null comment '驳回原因',
    created_at     datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '文档主表';

create index idx_audit_status
    on document (audit_status);

create index idx_author_id
    on document (author_id);

create index idx_team_id
    on document (team_id);

create index idx_visibility
    on document (visibility);

create table document_tag
(
    id          bigint auto_increment comment '主键'
        primary key,
    document_id bigint not null comment '文档ID（逻辑外键）',
    tag_id      bigint not null comment '标签ID（逻辑外键）',
    constraint uk_document_tag
        unique (document_id, tag_id)
)
    comment '文档-标签关联表';

create index idx_tag_id
    on document_tag (tag_id);

create table document_version
(
    id            bigint auto_increment comment '主键'
        primary key,
    document_id   bigint                             not null comment '关联文档ID（逻辑外键）',
    version       int                                not null comment '版本号',
    content       text                               not null comment '该版本全文',
    editor_id     bigint                             not null comment '编辑人ID（逻辑外键）',
    change_remark varchar(256)                       null comment '变更备注',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '文档版本表';

create index idx_document_id
    on document_version (document_id);

create table favorite
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '用户ID（逻辑外键）',
    document_id bigint                             not null comment '文档ID（逻辑外键）',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '收藏时间',
    constraint uk_user_document
        unique (user_id, document_id)
)
    comment '收藏表';

create index idx_document_id
    on favorite (document_id);

create table `like`
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '用户ID（逻辑外键）',
    document_id bigint                             not null comment '文档ID（逻辑外键）',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '点赞时间',
    constraint uk_user_document
        unique (user_id, document_id)
)
    comment '点赞表';

create index idx_document_id
    on `like` (document_id);

create table platform_admin
(
    id            bigint auto_increment comment '主键'
        primary key,
    admin_account varchar(64)                        not null comment '管理员独立登录账号',
    admin_pwd     varchar(128)                       not null comment '管理员独立加密密码',
    admin_type    tinyint  default 0                 not null comment '管理员类型：0=子管理员，1=超级管理员',
    status        tinyint  default 1                 not null comment '状态：0=禁用，1=启用',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_admin_account
        unique (admin_account)
)
    comment '平台管理员表';

create table tags
(
    id         bigint auto_increment comment '主键'
        primary key,
    name       varchar(64)                        not null comment '标签名称',
    team_id    bigint                             null comment '所属团队ID（逻辑外键）',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_team_tag
        unique (team_id, name)
)
    comment '标签表';

create table team
(
    id            bigint auto_increment comment '主键'
        primary key,
    name          varchar(64)                        not null comment '团队名称',
    owner_user_id bigint                             not null comment '团队队长ID（逻辑外键）',
    status        tinyint  default 0                 not null comment '团队状态：0=待创建（凑人中），1=已创建，2=已解散',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '团队表';

create index idx_owner_user_id
    on team (owner_user_id);

create table team_invite_apply
(
    id              bigint auto_increment comment '主键'
        primary key,
    team_id         bigint                             not null comment '关联团队ID（逻辑外键）',
    apply_user_id   bigint                             not null comment '发起申请/被邀请的用户ID（逻辑外键）',
    operate_user_id bigint                             not null comment '邀请人/队长审核人ID（逻辑外键）',
    type            tinyint                            not null comment '类型：0=队长邀请用户，1=用户主动申请',
    status          tinyint  default 0                 not null comment '状态：0=待处理，1=同意，2=拒绝，3=已过期',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '申请发起时间',
    handle_at       datetime                           null comment '处理时间'
)
    comment '团队邀请&申请表';

create index idx_apply_user_id
    on team_invite_apply (apply_user_id);

create index idx_status
    on team_invite_apply (status);

create index idx_team_id
    on team_invite_apply (team_id);

create table team_member
(
    id        bigint auto_increment comment '主键'
        primary key,
    team_id   bigint                             not null comment '团队ID（逻辑外键）',
    user_id   bigint                             not null comment '用户ID（逻辑外键）',
    team_role tinyint  default 0                 not null comment '团队内角色：0=普通成员，1=团队管理员',
    joined_at datetime default CURRENT_TIMESTAMP not null comment '加入时间',
    constraint uk_team_user
        unique (team_id, user_id)
)
    comment '团队成员关联表';

create index idx_user_id
    on team_member (user_id);

create table upload_session
(
    upload_id       varchar(64)                           not null
        primary key,
    document_id     bigint                                not null,
    total_chunks    int                                   not null,
    uploaded_chunks text                                  null,
    status          varchar(20) default 'uploading'       null comment '文件状态：uploading, completed, expired',
    created_at      datetime    default CURRENT_TIMESTAMP null,
    updated_at      datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '分块传输表';

create index idx_document_id
    on upload_session (document_id);

create index idx_status
    on upload_session (status);

create table user
(
    id         bigint auto_increment comment '主键'
        primary key,
    username   varchar(64)                        not null comment '登录账号，唯一',
    password   varchar(128)                       not null comment '加密密码',
    email      varchar(128)                       not null comment '邮箱',
    avatar     varchar(256)                       null comment '头像URL',
    status     tinyint  default 1                 not null comment '账号状态：0=禁用，1=启用',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_email
        unique (email),
    constraint uk_username
        unique (username)
)
    comment '用户表';

create table user_preference
(
    id         bigint auto_increment comment '主键'
        primary key,
    user_id    bigint                             not null comment '用户ID（逻辑外键）',
    pref_key   varchar(64)                        not null comment '偏好标识',
    pref_value text                               not null comment 'JSON格式偏好数据',
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_pref
        unique (user_id, pref_key)
)
    comment '用户偏好表';

create table user_session_relation
(
    id         bigint auto_increment comment '主键'
        primary key,
    user_id    bigint      not null comment '用户ID（逻辑外键）',
    session_id varchar(64) not null comment '会话ID（逻辑外键）',
    constraint uk_user_session
        unique (user_id, session_id)
)
    comment '用户-会话关联表';

create index idx_session_id
    on user_session_relation (session_id);


