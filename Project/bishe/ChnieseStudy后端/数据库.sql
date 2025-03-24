create table t_video
(
    video_id    bigint auto_increment                       comment '视频id' primary key,
    user_id     bigint                             not null comment '用户id',
    video_url   varchar(500)                       not null comment '视频链接',
    thumbnail   varchar(500)                       not null comment '封面链接',
    title       varchar(255)                       not null comment '视频标题',
    duration    varchar(255)                       not null comment '视频时长',
    description text                               null comment '视频简介',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '视频投稿记录表';

create table t_video_collection
(
    collection_id bigint auto_increment comment '主键id' primary key,
    video_id    bigint                             null comment '视频投稿id',
    user_id    bigint                             null comment '用户id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '视频收藏表';

create table t_video_comment
(
    video_comment_id            bigint auto_increment comment '主键id' primary key,
    video_id      bigint                             not null comment '视频id',
    user_id       bigint                             not null comment '用户id',
    comment       text                               not null comment '评论',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '视频评论表';

create table t_video_like
(
    video_like_id          bigint auto_increment comment '主键id' primary key,
    user_id     bigint                             not null comment '用户id',
    video_id    bigint                             not null comment '视频投稿id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '视频点赞表';

create table t_video_operation
(
    video_operation_id             bigint auto_increment comment '主键id'
primary key,
    user_id        bigint                             null comment '用户id',
    video_id       bigint                             null comment '视频id',
    operation_type varchar(5)                         null comment '操作类型:0点赞，1收藏',
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '用户操作表';

create table t_video_tag
(
    video_tag_id          bigint auto_increment comment '主键id' primary key,
    video_id    bigint                             not null comment '视频id',
    tag_id      bigint                             not null comment '标签id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '视频标签关联表';

create table t_video_view
(
    video_view_id          bigint auto_increment comment '主键id' primary key,
    video_id    bigint                             not null comment '视频id',
    user_id     bigint                             null comment '用户id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '视频观看记录表';

DROP TABLE IF EXISTS `t_bigtype`;

CREATE TABLE `t_bigtype` (
                             `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
                             `name` varchar(50) DEFAULT NULL COMMENT '视频大类名称',
                             `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                             `bigtype_image` varchar(255) DEFAULT NULL COMMENT '大类封面图片',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8;