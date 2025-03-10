
create database if not exists oj_database;

use oj_database;

drop table if exists oj_table;
create table oj_table(
    id int primary key auto_increment,
    title varchar(50) charset utf8,
    level varchar(50) charset utf8,
    description varchar(4096) charset utf8,
    templateCode varchar(4096) charset utf8,
    testCode varchar(4096) charset utf8
);