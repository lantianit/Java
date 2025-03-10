-- seata 演示数据库
drop
DATABASE if EXISTS seata_demo;
CREATE
DATABASE seata_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use
seata_demo;
-- 业务表【仓库表】
DROP TABLE IF EXISTS `storage`;
CREATE TABLE `storage`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT comment '主键',
    `commodity_code` varchar(255) DEFAULT NULL comment '商品编码',
    `count`          int(11) unsigned DEFAULT 0 comment '库存',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 业务表【订单表】
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT comment '编码',
    `user_id`        varchar(255) DEFAULT NULL comment '用户id',
    `commodity_code` varchar(255) DEFAULT NULL comment '商品编码',
    `count`          int(11) unsigned DEFAULT 0 comment '购买数量',
    `money`          int(11) unsigned DEFAULT 0 comment '花费金额',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 业务表【余额表】
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`
(
    `id`      int(11) NOT NULL AUTO_INCREMENT comment '主键',
    `user_id` varchar(255) DEFAULT NULL comment '用户id',
    `money`   int(11) unsigned DEFAULT 0 comment '账户余额',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 添加测试数据
insert into account(`user_id`,`money`) values('1','1000');
insert into storage(`commodity_code`,`count`) values('goods-001','10');

select * from `account`;

select * from `storage`;

