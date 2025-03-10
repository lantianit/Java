use seata_demo;
-- 仓库冻结表
CREATE TABLE storage_freeze(
xid varchar(128) NOT NULL primary key,
commodity_code varchar(256) DEFAULT NULL COMMENT '商品编号',
freeze_count  int(11) unsigned DEFAULT '0' COMMENT '冻结数量',
`state` int(11) DEFAULT NULL COMMENT '事务状态,0:try, 1:confirm, 2:cancel'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
