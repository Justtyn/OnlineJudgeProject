/*
 Navicat Premium Dump SQL

 Source Server         : Justyn's Macbook
 Source Server Type    : MySQL
 Source Server Version : 90200 (9.2.0)
 Source Host           : localhost:3306
 Source Schema         : OnlineJudge

 Target Server Type    : MySQL
 Target Server Version : 90200 (9.2.0)
 File Encoding         : 65001

 Date: 13/07/2025 12:38:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(255) DEFAULT NULL COMMENT '账号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `sex` varchar(255) DEFAULT NULL COMMENT '性别',
  `birth` varchar(255) DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(255) DEFAULT NULL COMMENT '电话',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `role` varchar(255) DEFAULT NULL COMMENT '身份标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` varchar(500) NOT NULL COMMENT '公告内容',
  `time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告表';

-- ----------------------------
-- Table structure for contest
-- ----------------------------
DROP TABLE IF EXISTS `contest`;
CREATE TABLE `contest` (
  `id` int NOT NULL COMMENT '编号',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `user_id` int NOT NULL COMMENT '发布者',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `status` varchar(255) NOT NULL COMMENT '状态',
  `rule` varchar(255) NOT NULL COMMENT '规则',
  PRIMARY KEY (`id`),
  KEY `竞赛-用户` (`user_id`),
  CONSTRAINT `竞赛-用户` FOREIGN KEY (`user_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for discuss
-- ----------------------------
DROP TABLE IF EXISTS `discuss`;
CREATE TABLE `discuss` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '讨论ID',
  `user_id` int NOT NULL COMMENT '发布者ID',
  `problem_id` int DEFAULT NULL COMMENT '关联题目ID',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `content` text COMMENT '讨论内容',
  `view_num` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  PRIMARY KEY (`id`),
  KEY `idx_discuss_user` (`user_id`),
  KEY `idx_discuss_problem` (`problem_id`),
  KEY `idx_discuss_create_time` (`create_time`),
  CONSTRAINT `fk_discuss_problem` FOREIGN KEY (`problem_id`) REFERENCES `oj_problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_discuss_user` FOREIGN KEY (`user_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for discuss_comment
-- ----------------------------
DROP TABLE IF EXISTS `discuss_comment`;
CREATE TABLE `discuss_comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `discuss_id` int NOT NULL COMMENT '所属讨论ID',
  `user_id` int NOT NULL COMMENT '评论者ID',
  `parent_comment_id` int DEFAULT NULL COMMENT '父评论ID，NULL=一级评论',
  `reply_to_user_id` int DEFAULT NULL COMMENT '回复给的用户ID（可选）',
  `content` text NOT NULL COMMENT '评论内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_discuss` (`discuss_id`),
  KEY `idx_comment_user` (`user_id`),
  KEY `idx_comment_parent` (`parent_comment_id`),
  KEY `fk_comment_reply_to_user` (`reply_to_user_id`),
  CONSTRAINT `fk_comment_discuss` FOREIGN KEY (`discuss_id`) REFERENCES `discuss` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `discuss_comment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_reply_to_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `student` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for homework
-- ----------------------------
DROP TABLE IF EXISTS `homework`;
CREATE TABLE `homework` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `class_id` int NOT NULL COMMENT '所属班级',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `作业表-班级表` (`class_id`),
  CONSTRAINT `作业表-班级表` FOREIGN KEY (`class_id`) REFERENCES `score` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- ----------------------------
-- Table structure for homework_problem
-- ----------------------------
DROP TABLE IF EXISTS `homework_problem`;
CREATE TABLE `homework_problem` (
  `homework_id` int NOT NULL COMMENT '作业ID',
  `problem_id` int NOT NULL COMMENT '题目ID',
  `ac_count` int DEFAULT '0' COMMENT '通过数',
  `submit_count` int DEFAULT '0' COMMENT '提交数',
  PRIMARY KEY (`homework_id`,`problem_id`),
  KEY `作业题目表-题目表` (`problem_id`),
  CONSTRAINT `作业题目表-作业表` FOREIGN KEY (`homework_id`) REFERENCES `homework` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `作业题目表-题目表` FOREIGN KEY (`problem_id`) REFERENCES `oj_problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业题目关联表';

-- ----------------------------
-- Table structure for oj_problem
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem`;
CREATE TABLE `oj_problem` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '题号',
  `name` varchar(255) NOT NULL COMMENT '问题名称',
  `setter` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出题人',
  `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出题时间',
  `ac_count` int DEFAULT '0' COMMENT '通过数量',
  `submit_count` int DEFAULT '0' COMMENT '提交数量',
  `desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目描述',
  `desc_input` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '输入描述',
  `desc_output` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '输出描述',
  `sample_input` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '输入样例',
  `sample_output` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '输出样例',
  `hint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提示说明',
  `daily_challenge` varchar(255) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28590 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for oj_status
-- ----------------------------
DROP TABLE IF EXISTS `oj_status`;
CREATE TABLE `oj_status` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `problem_id` int NOT NULL COMMENT '题目ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `username` varchar(255) DEFAULT NULL COMMENT '账号名称',
  `time` varchar(255) DEFAULT NULL COMMENT '时间',
  `creat_time` varchar(255) DEFAULT NULL COMMENT '提交时间',
  `language` varchar(255) DEFAULT NULL COMMENT '语言',
  `memory` varchar(255) DEFAULT NULL COMMENT '内存',
  `status` varchar(255) DEFAULT NULL COMMENT '状态',
  `code` text COMMENT '代码',
  `output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输出信息',
  PRIMARY KEY (`id`,`problem_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=148 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for score
-- ----------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `creator_id` int DEFAULT NULL COMMENT '创建者',
  `homework_quantity` int NOT NULL DEFAULT '0' COMMENT '作业数量',
  PRIMARY KEY (`id`),
  KEY `班级表-用户表` (`creator_id`),
  CONSTRAINT `班级表-用户表` FOREIGN KEY (`creator_id`) REFERENCES `admin` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- ----------------------------
-- Table structure for solution
-- ----------------------------
DROP TABLE IF EXISTS `solution`;
CREATE TABLE `solution` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '题解ID',
  `user_id` int NOT NULL COMMENT '发布者ID',
  `problem_id` int NOT NULL COMMENT '题目ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '题解内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `like_num` int DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`),
  KEY `发布者` (`user_id`),
  KEY `问题` (`problem_id`),
  CONSTRAINT `发布者` FOREIGN KEY (`user_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `问题` FOREIGN KEY (`problem_id`) REFERENCES `oj_problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `sex` varchar(255) DEFAULT NULL COMMENT '性别',
  `birth` varchar(255) DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(255) DEFAULT NULL COMMENT '电话',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份标识',
  `background` varchar(255) DEFAULT NULL COMMENT '背景图片',
  `ac` int DEFAULT '0' COMMENT '通过数',
  `submit` int DEFAULT '0' COMMENT '提交数',
  `school` varchar(255) DEFAULT NULL COMMENT '学校',
  `score` int DEFAULT '0' COMMENT '得分',
  `last_login_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '上次登录时间',
  `last_language` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '上次提交的语言',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  `last_visit_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次访问时间',
  `class_id` int DEFAULT NULL COMMENT '所属班级ID',
  `daily_challenge` varchar(255) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `用户表-班级表` (`class_id`),
  CONSTRAINT `用户表-班级表` FOREIGN KEY (`class_id`) REFERENCES `score` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
