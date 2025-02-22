    -- 创建数据库
    create database if not exists flower_picture;

    -- 切换库
    use flower_picture;

    -- 创建用户表
    create table if not exists user
    (
        id           bigint auto_increment comment 'id' primary key,
        userAccount  varchar(256)                           not null comment '账号',
        userPassword varchar(512)                           not null comment '密码',
        userName     varchar(256)                           null comment '用户昵称',
        userAvatar   varchar(1024)                          null comment '用户头像',
        userProfile  varchar(512)                           null comment '用户简介',
        userRole     varchar(256) default 'user'            not null comment '用户角色 user/admin',
        editTime     dateTime     default CURRENT_TIMESTAMP not null comment '编辑时间',
        createTime   dateTime     default CURRENT_TIMESTAMP not null comment '新建时间',
        updateTime   dateTime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
        isDelete     tinyint      default 0                 not null comment '是否删除',
        UNIQUE KEY uk_userAccount (userAccount),
        INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;

    -- 创建图片表
    create table if not exists pictures
    (
        id           bigint auto_increment comment 'id'     primary key,
        url          varchar(512)                           not null comment '图片url',
        name         varchar(128)                           not null comment '图片名',
        introduction varchar(512)                           null comment '简介',
        category     varchar(64)                            null comment '分类',
        tags         varchar(512)                           null comment '标签(json数组)',
        picSize      bigint                                 null comment '图片体积',
        picWidth     int                                    null comment '图片宽度',
        picHeight    int                                    null comment '图片高度',
        picScale     double                                 null comment '图片宽高比例',
        picFormat    varchar(32)                            null comment '图片格式',
        userID       bigint                                 not null comment '创建用户id',
        editTime     dateTime     default CURRENT_TIMESTAMP not null comment '编辑时间',
        createTime   dateTime     default CURRENT_TIMESTAMP not null comment '新建时间',
        updateTime   dateTime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
        isDelete     tinyint      default 0                 not null comment '是否删除',
        INDEX idx_name (name),
        INDEX idx_introduction (introduction),
        INDEX idx_category (category),
        INDEX idx_tags (tags),
        Index idx_userId(userID)
    ) comment '用户' collate = utf8mb4_unicode_ci;

    -- 增加Picture字段
    ALTER TABLE pictures
        -- 添加字段
        ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '0-待审核; 1-通过; 2-拒绝',
        ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
        ADD COLUMN reviewId BIGINT NULL COMMENT '审核人ID',
        ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';

    -- 基于reviewStatus建立索引
    create INDEX idx_reviewStatus ON pictures (reviewStatus);

    -- 增加Picture字段
    ALTER TABLE pictures
        -- 添加字段
        ADD COLUMN thumbnailUrl VARCHAR(512)  NULL COMMENT '缩略图url';

    -- 空间表
    create table if not exists space
    (
        id         bigint auto_increment comment 'id' primary key,
        spaceName  varchar(128)                       null comment '空间名称',
        spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
        maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
        maxCount   bigint   default 0                 null comment '空间图片的最大数量',
        totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
        totalCount bigint   default 0                 null comment '当前空间下的图片数量',
        userId     bigint                             not null comment '创建用户 id',
        createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
        editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
        updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
        isDelete   tinyint  default 0                 not null comment '是否删除',
        -- 索引设计
        index idx_userId (userId),        -- 提升基于用户的查询效率
        index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
        index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
    ) comment '空间' collate = utf8mb4_unicode_ci;

    -- 添加图片主色调
    ALTER TABLE pictures
        ADD COLUMN picColor varchar(16) null comment '图片主色调';
