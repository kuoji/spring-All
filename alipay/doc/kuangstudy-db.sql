/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50733
Source Host           : localhost:3306
Source Database       : kuangstudy-db

Target Server Type    : MYSQL
Target Server Version : 50733
File Encoding         : 65001

Date: 2021-04-01 19:48:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for kss_courses
-- ----------------------------
DROP TABLE IF EXISTS `kss_courses`;
CREATE TABLE `kss_courses` (
  `courseid` varchar(32) NOT NULL COMMENT '课程唯一id',
  `title` varchar(100) DEFAULT NULL COMMENT '课程标题',
  `intro` varchar(500) DEFAULT NULL COMMENT '课程简短介绍',
  `img` varchar(300) DEFAULT NULL COMMENT '课程封面地址',
  `price` decimal(10,2) DEFAULT NULL COMMENT '课程的活动价',
  `status` int(1) DEFAULT NULL COMMENT '状态:已发布/未发布',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`courseid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of kss_courses
-- ----------------------------
INSERT INTO `kss_courses` VALUES ('1317503462556848129', '预科阶段', '学习编程之前你要了解的知识！', '/assert/course/c1/02.jpg', '0.01', '1', '2020-10-18 00:31:18', '2021-04-01 10:56:38');
INSERT INTO `kss_courses` VALUES ('1317503769349214209', '入门环境搭建', '工欲善其事，必先利其器！', '/assert/course/c1/03.jpg', '0.01', '1', '2020-10-18 00:32:31', '2021-04-01 10:53:10');
INSERT INTO `kss_courses` VALUES ('1317504142650658818', '基础语法学习', '基础决定你未来的高度！', '/assert/course/c1/04.jpg', '0.01', '1', '2020-10-18 00:34:00', '2021-04-01 10:54:18');
INSERT INTO `kss_courses` VALUES ('1317504447027105793', '流程控制学习', '程序的本质就是这些！', '/assert/course/c1/05.jpg', '0.01', '1', '2020-10-18 00:35:13', '2021-04-01 10:56:03');
INSERT INTO `kss_courses` VALUES ('1317504610634321921', '方法详解', '封装的思想！', '/assert/course/c1/06.jpg', '0.01', '1', '2020-10-18 00:35:52', '2021-04-01 10:55:04');
INSERT INTO `kss_courses` VALUES ('1317504817342205954', '数组详解', '最简单的数据结构！', '/assert/course/c1/07.jpg', '0.01', '1', '2020-10-18 00:35:52', '2020-10-18 00:35:52');
INSERT INTO `kss_courses` VALUES ('1317504988834713602', '面向对象编程', 'Java的精髓OOP！', '/assert/course/c1/08.jpg', '0.01', '1', '2020-10-18 00:35:52', '2020-10-18 00:35:52');
INSERT INTO `kss_courses` VALUES ('1377518279077142529', '第三方支付课程-支付宝', '第三方支付课程-支付宝', '/assert/course/c10/07.jpg', '0.01', '1', '2020-10-18 00:18:08', '2021-04-01 10:54:25');

-- ----------------------------
-- Table structure for kss_order_detail
-- ----------------------------
DROP TABLE IF EXISTS `kss_order_detail`;
CREATE TABLE `kss_order_detail` (
  `id` bigint(20) NOT NULL,
  `courseid` varchar(20) DEFAULT NULL,
  `coursetitle` varchar(255) DEFAULT NULL,
  `courseimg` varchar(255) DEFAULT NULL,
  `userid` varchar(32) DEFAULT NULL,
  `ordernumber` varchar(100) DEFAULT NULL,
  `tradeno` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `price` varchar(10) DEFAULT NULL,
  `paymethod` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of kss_order_detail
-- ----------------------------
INSERT INTO `kss_order_detail` VALUES ('1377561070331342849', '1317503462556848129', '预科阶段', '/assert/course/c1/02.jpg', '1', '2021040117565301', '2021040122001474081439646913', '2021-04-01 17:58:48', '2021-04-01 17:58:48', '飞哥', '0.01', '1');
INSERT INTO `kss_order_detail` VALUES ('1377561833455484929', '1317503769349214209', '入门环境搭建', '/assert/course/c1/03.jpg', '1', '2021040118015901', '2021040122001474081440101072', '2021-04-01 18:01:50', '2021-04-01 18:01:50', '飞哥', '0.01', '1');
INSERT INTO `kss_order_detail` VALUES ('1377562728612233218', '1317503769349214209', '入门环境搭建', '/assert/course/c1/03.jpg', '1', '2021040118053301', '2021040122001474081440405818', '2021-04-01 18:05:23', '2021-04-01 18:05:23', '飞哥', '0.01', '1');
INSERT INTO `kss_order_detail` VALUES ('1377564997252657153', '1317504142650658818', '基础语法学习', '/assert/course/c1/04.jpg', '1', '2021040118134201', '2021040122001474081440148822', '2021-04-01 18:14:24', '2021-04-01 18:14:24', '飞哥', '0.01', '1');
