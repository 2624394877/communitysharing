/*
 Navicat Premium Data Transfer

 Source Server         : MySQL8.0.17
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : communitysharing

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 14/03/2026 16:08:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_channel
-- ----------------------------
DROP TABLE IF EXISTS `t_channel`;
CREATE TABLE `t_channel`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '频道名称',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '频道表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_channel
-- ----------------------------
INSERT INTO `t_channel` VALUES (1, '美食', '2026-01-30 08:45:36', '2026-01-30 08:45:36', b'0');
INSERT INTO `t_channel` VALUES (2, '娱乐', '2026-01-30 08:45:45', '2026-01-30 08:45:45', b'0');
INSERT INTO `t_channel` VALUES (3, '游戏', '2026-03-07 12:53:23', '2026-03-07 12:53:23', b'0');
INSERT INTO `t_channel` VALUES (4, '音乐', '2026-03-07 12:54:35', '2026-03-07 12:54:35', b'0');

-- ----------------------------
-- Table structure for t_channel_topic_rel
-- ----------------------------
DROP TABLE IF EXISTS `t_channel_topic_rel`;
CREATE TABLE `t_channel_topic_rel`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` bigint(0) UNSIGNED NOT NULL COMMENT '频道ID',
  `topic_id` bigint(0) UNSIGNED NOT NULL COMMENT '话题ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '频道-话题关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_channel_topic_rel
-- ----------------------------
INSERT INTO `t_channel_topic_rel` VALUES (1, 1, 2, '2026-01-30 08:49:33', '2026-01-30 08:49:33');
INSERT INTO `t_channel_topic_rel` VALUES (2, 2, 1, '2026-01-30 08:49:33', '2026-01-30 08:49:33');
INSERT INTO `t_channel_topic_rel` VALUES (3, 2, 3, '2026-01-30 16:19:39', '2026-01-30 16:19:39');
INSERT INTO `t_channel_topic_rel` VALUES (4, 3, 3, '2026-03-07 12:54:17', '2026-03-07 12:54:17');
INSERT INTO `t_channel_topic_rel` VALUES (5, 4, 4, '2026-03-07 12:55:00', '2026-03-07 12:55:00');
INSERT INTO `t_channel_topic_rel` VALUES (6, 1, 4, '2026-03-07 12:55:25', '2026-03-07 12:55:25');

-- ----------------------------
-- Table structure for t_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的笔记ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '发布者用户ID',
  `content_uuid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '评论内容UUID',
  `is_content_empty` bit(1) NOT NULL DEFAULT b'0' COMMENT '内容是否为空(0：不为空 1：为空)',
  `image_url` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '评论附加图片URL',
  `level` tinyint(0) NOT NULL DEFAULT 1 COMMENT '级别(1：一级评论 2：二级评论)',
  `reply_total` bigint(0) UNSIGNED NULL DEFAULT 0 COMMENT '评论被回复次数，仅一级评论需要',
  `like_total` bigint(0) NULL DEFAULT NULL,
  `parent_id` bigint(0) UNSIGNED NULL DEFAULT 0 COMMENT '父ID (若是对笔记的评论，则此字段存储笔记ID; 若是二级评论，则此字段存储一级评论的ID)',
  `reply_comment_id` bigint(0) UNSIGNED NULL DEFAULT 0 COMMENT '回复哪个的评论 (0表示是对笔记的评论，若是对他人评论的回复，则存储回复评论的ID)',
  `reply_user_id` bigint(0) UNSIGNED NULL DEFAULT 0 COMMENT '回复的哪个用户, 存储用户ID',
  `is_top` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否置顶(0：不置顶 1：置顶)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `child_comment_total` bigint(0) UNSIGNED NULL DEFAULT 0 COMMENT '二级评论总数（只有一级评论才需要统计）',
  `heat` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '评论热度',
  `first_reply_comment_id` bigint(0) UNSIGNED NULL DEFAULT 0 COMMENT '最早回复的评论ID (只有一级评论需要)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_content_id`(`content_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_reply_comment_id`(`reply_comment_id`) USING BTREE,
  INDEX `idx_reply_user_id`(`reply_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1018026 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_comment
-- ----------------------------
INSERT INTO `t_comment` VALUES (1018022, 2017271718413860910, 10, '10ed4421-1b4b-4857-92c6-30940312ab4d', b'0', '', 1, 0, 0, 2017271718413860910, 0, 0, b'0', '2026-03-13 17:35:50', '2026-03-13 09:36:25', 1, 0.30, 1018023);
INSERT INTO `t_comment` VALUES (1018023, 2017271718413860910, 10, 'f856887c-9606-42ea-84d0-cd473df87de6', b'0', '', 2, 0, 0, 1018022, 1018022, 10, b'0', '2026-03-13 17:36:25', '2026-03-13 17:36:25', 0, 0.00, 0);
INSERT INTO `t_comment` VALUES (1018024, 2017271718413860910, 10, '62b3d0a9-65cc-4e01-9c9c-ae61119baa63', b'0', 'http://127.0.0.1:9000/communitysharing/963df3b47c604d3b85fd4848c1838f27.png', 1, 0, 0, 2017271718413860910, 0, 0, b'0', '2026-03-13 17:37:24', '2026-03-13 17:37:24', 0, 0.00, 0);
INSERT INTO `t_comment` VALUES (1018025, 2026507475867402296, 10, 'c3b58083-7d23-431f-a275-085c5d28ab71', b'0', '', 1, 0, 1, 2026507475867402296, 0, 0, b'0', '2026-03-13 17:39:05', '2026-03-13 09:39:54', 0, 0.00, 0);

-- ----------------------------
-- Table structure for t_comment_like
-- ----------------------------
DROP TABLE IF EXISTS `t_comment_like`;
CREATE TABLE `t_comment_like`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `comment_id` bigint(0) NOT NULL COMMENT '评论ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_comment_id`(`user_id`, `comment_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_comment_like
-- ----------------------------
INSERT INTO `t_comment_like` VALUES (22, 10, 1018001, '2026-03-13 15:35:51');
INSERT INTO `t_comment_like` VALUES (23, 10, 1014015, '2026-03-13 15:57:09');
INSERT INTO `t_comment_like` VALUES (24, 10, 1018012, '2026-03-13 15:57:23');
INSERT INTO `t_comment_like` VALUES (25, 10, 1014014, '2026-03-13 16:10:05');
INSERT INTO `t_comment_like` VALUES (26, 10, 1018025, '2026-03-13 17:39:53');

-- ----------------------------
-- Table structure for t_content
-- ----------------------------
DROP TABLE IF EXISTS `t_content`;
CREATE TABLE `t_content`  (
  `id` bigint(0) UNSIGNED NOT NULL COMMENT '主键ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `is_content_empty` bit(1) NOT NULL DEFAULT b'0' COMMENT '内容是否为空(0：不为空 1：空)',
  `creator_id` bigint(0) UNSIGNED NOT NULL COMMENT '发布者ID',
  `topic_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '话题ID',
  `topic_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '话题名称',
  `is_top` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否置顶(0：未置顶 1：置顶)',
  `type` tinyint(0) NULL DEFAULT 0 COMMENT '类型(0：图文 1：视频)',
  `img_uris` varchar(660) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '笔记图片链接(逗号隔开)',
  `video_uri` varchar(330) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频链接',
  `file_uris` varchar(660) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件链接',
  `url_uris` varchar(660) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '外部链接',
  `visible` tinyint(0) NULL DEFAULT 0 COMMENT '可见范围(0：公开,所有人可见 1：仅对自己可见)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态(0：待审核 1：正常展示 2：被删除(逻辑删除) 3：被下架)',
  `content_uuid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '发布的内容UUID',
  `channel_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '频道ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  INDEX `idx_topic_id`(`topic_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_channel_id`(`channel_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_content
-- ----------------------------
INSERT INTO `t_content` VALUES (2017271718413860910, '推荐个音乐给大家听', b'0', 10, 4, '音乐视频推荐', b'1', 1, 'http://127.0.0.1:9000/communitysharing/bf356c6d5bfd4c6e9ea129654bcd2a14.png', 'http://127.0.0.1:9000/communitysharing/cdfebdd16ac04479b602c0885017f371.mp3,http://127.0.0.1:9000/communitysharing/9fe9d6f46a704477bf81bf281045c528.mp3', NULL, NULL, 0, '2026-01-31 00:20:32', '2026-03-10 23:53:00', 1, '77999e64-43d2-4344-9e56-cf5829a6ac07', 4);
INSERT INTO `t_content` VALUES (2024441229604814885, '内容计数接口测试1', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-02-19 19:09:37', '2026-02-19 19:09:37', 1, '8153b3f1-16e3-4581-a624-309aa907e6c3', 3);
INSERT INTO `t_content` VALUES (2026507475867402296, '短视频分享', b'0', 10, 4, '音乐视频推荐', b'0', 1, 'http://127.0.0.1:9000/communitysharing/0843edd07d2f465dba51b8b4b8d508bc.jpg', 'http://127.0.0.1:9000/communitysharing/79b06afe48ce4da18cc8d7687f204643.mp4,http://127.0.0.1:9000/communitysharing/5f969bd986be4ef687a30e6f6b9b6540.mp4', NULL, NULL, 0, '2026-02-25 12:00:09', '2026-03-11 10:31:17', 1, '14bbfa19-5899-4030-9f83-2b4242c3fb0e', 4);
INSERT INTO `t_content` VALUES (2029807716507058190, '大家快来看呐！', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-06 14:34:09', '2026-03-06 14:34:09', 1, 'e95636c1-05ed-4bcf-8f9d-469ab715a33f', 3);
INSERT INTO `t_content` VALUES (2030475713500413986, '不好了', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 10:48:32', '2026-03-08 10:48:32', 1, '97a53c17-c0e4-4df1-a89c-900987b0ad5f', 3);
INSERT INTO `t_content` VALUES (2030532839480492057, '增加频道字段', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 14:35:31', '2026-03-08 14:35:31', 1, 'a4666913-4e4b-431d-af3d-bf3ea8bcd2b1', 3);
INSERT INTO `t_content` VALUES (2030532858019315811, '增加频道字段', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 14:35:34', '2026-03-08 14:35:34', 1, 'cf9ca961-36a0-4c4f-b04b-fecbdaf61f9e', 3);
INSERT INTO `t_content` VALUES (2030532861882269726, '增加频道字段', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 14:35:34', '2026-03-08 14:35:34', 1, '40f14cd2-ab3e-4864-8c23-8aadea71f70e', 3);
INSERT INTO `t_content` VALUES (2030532864977666069, '增加频道字段', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 14:35:35', '2026-03-08 14:35:35', 1, 'dd52dc3b-a740-4da1-86fa-44a9b6a96a2d', 3);
INSERT INTO `t_content` VALUES (2030532867884318779, '增加频道字段', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 14:35:36', '2026-03-08 14:35:36', 1, '5424a7e4-5282-48a0-8016-dfd1ebf6bf48', 3);
INSERT INTO `t_content` VALUES (2030533198835875900, '增加频道字段', b'0', 10, 3, '热门游戏推荐', b'0', 0, 'http://127.0.0.1:9000/communitysharing/c5f4244d3c3f4802957891696e3a4f29.png', NULL, NULL, NULL, 0, '2026-03-08 14:36:55', '2026-03-08 14:36:55', 1, '48ed3a54-5544-420e-bd56-6017a0952343', 3);

-- ----------------------------
-- Table structure for t_content_collection
-- ----------------------------
DROP TABLE IF EXISTS `t_content_collection`;
CREATE TABLE `t_content_collection`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `content_id` bigint(0) NOT NULL COMMENT '内容ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '收藏状态(0：取消收藏 1：收藏)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_content_id`(`user_id`, `content_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '内容收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_content_collection
-- ----------------------------
INSERT INTO `t_content_collection` VALUES (49, 10, 2017271718413860910, '2026-03-12 13:32:44', 0);
INSERT INTO `t_content_collection` VALUES (50, 10, 2026507475867402296, '2026-03-05 01:53:08', 1);

-- ----------------------------
-- Table structure for t_content_count
-- ----------------------------
DROP TABLE IF EXISTS `t_content_count`;
CREATE TABLE `t_content_count`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  `like_total` bigint(0) NULL DEFAULT 0 COMMENT '获得点赞总数',
  `collect_total` bigint(0) NULL DEFAULT 0 COMMENT '获得收藏总数',
  `comment_total` bigint(0) NULL DEFAULT 0 COMMENT '被评论总数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 294 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '内容计数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_content_count
-- ----------------------------
INSERT INTO `t_content_count` VALUES (79, 2017271718413860910, 1, 0, 3);
INSERT INTO `t_content_count` VALUES (81, 2026507475867402296, 1, 1, 1);
INSERT INTO `t_content_count` VALUES (218, 2024441229604814885, 1, 0, 0);

-- ----------------------------
-- Table structure for t_content_like
-- ----------------------------
DROP TABLE IF EXISTS `t_content_like`;
CREATE TABLE `t_content_like`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `content_id` bigint(0) NOT NULL COMMENT '内容ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '点赞状态(0：取消点赞 1：点赞)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_content_id`(`user_id`, `content_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 86 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '内容点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_content_like
-- ----------------------------
INSERT INTO `t_content_like` VALUES (47, 10, 2017271718413860910, '2026-02-27 00:08:00', 1);
INSERT INTO `t_content_like` VALUES (48, 10, 2026507475867402296, '2026-02-28 20:43:46', 1);
INSERT INTO `t_content_like` VALUES (63, 10, 2024441229604814885, '2026-03-05 19:07:00', 1);

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20250219_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20250219_0`;
CREATE TABLE `t_data_align_content_collect_count_temp_20250219_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20250219_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260307_0`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260307_1`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260307_2`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260310_0`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260310_1`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260310_2`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260311_0`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260311_1`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260311_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_collect_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_collect_count_temp_20260311_2`;
CREATE TABLE `t_data_align_content_collect_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '笔记ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_collect_count_temp_20260311_2
-- ----------------------------
INSERT INTO `t_data_align_content_collect_count_temp_20260311_2` VALUES (1, 2017271718413860910);

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20250219_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20250219_0`;
CREATE TABLE `t_data_align_content_like_count_temp_20250219_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20250219_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260307_0`;
CREATE TABLE `t_data_align_content_like_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260307_1`;
CREATE TABLE `t_data_align_content_like_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260307_2`;
CREATE TABLE `t_data_align_content_like_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260310_0`;
CREATE TABLE `t_data_align_content_like_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260310_1`;
CREATE TABLE `t_data_align_content_like_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260310_2`;
CREATE TABLE `t_data_align_content_like_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260311_0`;
CREATE TABLE `t_data_align_content_like_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260311_1`;
CREATE TABLE `t_data_align_content_like_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260311_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_like_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_like_count_temp_20260311_2`;
CREATE TABLE `t_data_align_content_like_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_id` bigint(0) UNSIGNED NOT NULL COMMENT '内容ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_id`(`content_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_like_count_temp_20260311_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20250224_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20250224_2`;
CREATE TABLE `t_data_align_content_publish_count_temp_20250224_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20250224_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260307_0`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260307_1`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260307_2`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260310_0`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260310_1`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260310_2`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260311_0`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260311_1`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260311_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_content_publish_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_content_publish_count_temp_20260311_2`;
CREATE TABLE `t_data_align_content_publish_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户发布数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_content_publish_count_temp_20260311_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20250219_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20250219_0`;
CREATE TABLE `t_data_align_fans_count_temp_20250219_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20250219_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260307_0`;
CREATE TABLE `t_data_align_fans_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260307_1`;
CREATE TABLE `t_data_align_fans_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260307_2`;
CREATE TABLE `t_data_align_fans_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260310_0`;
CREATE TABLE `t_data_align_fans_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260310_1`;
CREATE TABLE `t_data_align_fans_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260310_2`;
CREATE TABLE `t_data_align_fans_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260311_0`;
CREATE TABLE `t_data_align_fans_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260311_1`;
CREATE TABLE `t_data_align_fans_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260311_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_fans_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_fans_count_temp_20260311_2`;
CREATE TABLE `t_data_align_fans_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：粉丝数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_fans_count_temp_20260311_2
-- ----------------------------
INSERT INTO `t_data_align_fans_count_temp_20260311_2` VALUES (1, 2029648933701025799);

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20250219_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20250219_0`;
CREATE TABLE `t_data_align_following_count_temp_20250219_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20250219_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260307_0`;
CREATE TABLE `t_data_align_following_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260307_1`;
CREATE TABLE `t_data_align_following_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260307_2`;
CREATE TABLE `t_data_align_following_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260310_0`;
CREATE TABLE `t_data_align_following_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260310_1`;
CREATE TABLE `t_data_align_following_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260310_2`;
CREATE TABLE `t_data_align_following_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260311_0`;
CREATE TABLE `t_data_align_following_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260311_1`;
CREATE TABLE `t_data_align_following_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260311_1
-- ----------------------------
INSERT INTO `t_data_align_following_count_temp_20260311_1` VALUES (1, 10);

-- ----------------------------
-- Table structure for t_data_align_following_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_following_count_temp_20260311_2`;
CREATE TABLE `t_data_align_following_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：关注数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_following_count_temp_20260311_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20250219_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20250219_0`;
CREATE TABLE `t_data_align_user_collect_count_temp_20250219_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20250219_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260307_0`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260307_1`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260307_2`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260310_0`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260310_1`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260310_2`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260311_0`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260311_1`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260311_1
-- ----------------------------
INSERT INTO `t_data_align_user_collect_count_temp_20260311_1` VALUES (1, 10);

-- ----------------------------
-- Table structure for t_data_align_user_collect_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_collect_count_temp_20260311_2`;
CREATE TABLE `t_data_align_user_collect_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得收藏数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_collect_count_temp_20260311_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20250219_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20250219_0`;
CREATE TABLE `t_data_align_user_like_count_temp_20250219_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20250219_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260307_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260307_0`;
CREATE TABLE `t_data_align_user_like_count_temp_20260307_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260307_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260307_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260307_1`;
CREATE TABLE `t_data_align_user_like_count_temp_20260307_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260307_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260307_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260307_2`;
CREATE TABLE `t_data_align_user_like_count_temp_20260307_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260307_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260310_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260310_0`;
CREATE TABLE `t_data_align_user_like_count_temp_20260310_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260310_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260310_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260310_1`;
CREATE TABLE `t_data_align_user_like_count_temp_20260310_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260310_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260310_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260310_2`;
CREATE TABLE `t_data_align_user_like_count_temp_20260310_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260310_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260311_0
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260311_0`;
CREATE TABLE `t_data_align_user_like_count_temp_20260311_0`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260311_0
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260311_1
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260311_1`;
CREATE TABLE `t_data_align_user_like_count_temp_20260311_1`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260311_1
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_align_user_like_count_temp_20260311_2
-- ----------------------------
DROP TABLE IF EXISTS `t_data_align_user_like_count_temp_20260311_2`;
CREATE TABLE `t_data_align_user_like_count_temp_20260311_2`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据对齐日增量表：用户获得点赞数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_align_user_like_count_temp_20260311_2
-- ----------------------------

-- ----------------------------
-- Table structure for t_fans
-- ----------------------------
DROP TABLE IF EXISTS `t_fans`;
CREATE TABLE `t_fans`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `fans_user_id` bigint(0) UNSIGNED NOT NULL COMMENT '粉丝的用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_fans_user_id`(`user_id`, `fans_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21016 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户粉丝表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_fans
-- ----------------------------

-- ----------------------------
-- Table structure for t_following
-- ----------------------------
DROP TABLE IF EXISTS `t_following`;
CREATE TABLE `t_following`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `following_user_id` bigint(0) UNSIGNED NOT NULL COMMENT '关注的用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_following_user_id`(`user_id`, `following_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30020 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户关注表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_following
-- ----------------------------

-- ----------------------------
-- Table structure for t_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_permission`;
CREATE TABLE `t_permission`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '父ID',
  `name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `type` tinyint(0) UNSIGNED NOT NULL COMMENT '类型(1：目录 2：菜单 3：按钮)',
  `menu_url` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '菜单路由',
  `menu_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '菜单图标',
  `sort` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '管理系统中的显示顺序',
  `permission_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态(0：启用；1：禁用)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_permission
-- ----------------------------
INSERT INTO `t_permission` VALUES (1, 0, '发布笔记', 3, '', '', 1, 'app:note:publish', 0, '2025-12-28 12:12:12', '2025-12-28 12:12:12', b'0');
INSERT INTO `t_permission` VALUES (2, 0, '发布评论', 3, '', '', 2, 'app:comment:publish', 0, '2025-12-28 12:12:32', '2025-12-28 12:12:32', b'0');
INSERT INTO `t_permission` VALUES (3, 0, '测试', 3, '', '', 3, 'test', 0, '2026-01-05 14:23:00', '2026-01-05 14:23:00', b'0');

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名',
  `role_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色唯一标识',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态(0：启用 1：禁用)',
  `sort` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '管理系统中的显示顺序',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '最后一次更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO `t_role` VALUES (1, '普通用户', 'common_user', 0, 1, '', '2026-01-03 13:06:05', '2026-01-03 13:06:05', b'0');

-- ----------------------------
-- Table structure for t_role_permission_rel
-- ----------------------------
DROP TABLE IF EXISTS `t_role_permission_rel`;
CREATE TABLE `t_role_permission_rel`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(0) UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` bigint(0) UNSIGNED NOT NULL COMMENT '权限ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_role_permission_rel
-- ----------------------------
INSERT INTO `t_role_permission_rel` VALUES (1, 1, 1, '2025-12-28 12:19:49', '2025-12-28 12:19:49', b'0');
INSERT INTO `t_role_permission_rel` VALUES (2, 1, 2, '2025-12-28 12:19:49', '2025-12-28 12:19:49', b'0');

-- ----------------------------
-- Table structure for t_topic
-- ----------------------------
DROP TABLE IF EXISTS `t_topic`;
CREATE TABLE `t_topic`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题名称',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_topic
-- ----------------------------
INSERT INTO `t_topic` VALUES (1, '高分美剧推荐', '2026-01-30 08:47:32', '2026-01-30 08:47:32', b'0');
INSERT INTO `t_topic` VALUES (2, '周边美食推荐', '2026-01-30 08:47:32', '2026-01-30 08:47:32', b'0');
INSERT INTO `t_topic` VALUES (3, '热门游戏推荐', '2026-01-30 16:18:40', '2026-01-30 16:18:40', b'0');
INSERT INTO `t_topic` VALUES (4, '音乐视频推荐', '2026-02-02 10:04:13', '2026-02-02 10:04:13', b'0');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `communitysharing_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UID号(唯一凭证)',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `background_img` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '背景图',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `sex` tinyint(0) NULL DEFAULT 0 COMMENT '性别(0：女 1：男)',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态(0：启用 1：禁用)',
  `introduction` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_communitysharing_id`(`communitysharing_id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE,
  UNIQUE INDEX `uK_email`(`email`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2029648933701025800 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (10, '10000012', '$2a$10$eBuLciNDQSi40j36Om0DOeXcSxKnwVATD0XRaCE9XGy39cIVvaJsK', '星陨落月', 'http://127.0.0.1:9000/communitysharing/c0373cc462f24bf28ef73ffd3ec037a7.png', NULL, 'http://127.0.0.1:9000/communitysharing/e1e8d7ed649e40f092dec6979d109f9b.jpg', 'yy3377641269@163.com', '13870065967', 1, 0, '微服务全栈开发项目', '2026-01-16 13:57:42', '2026-03-06 17:28:43', b'0');

-- ----------------------------
-- Table structure for t_user_count
-- ----------------------------
DROP TABLE IF EXISTS `t_user_count`;
CREATE TABLE `t_user_count`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `fans_total` bigint(0) NULL DEFAULT 0 COMMENT '粉丝总数',
  `following_total` bigint(0) NULL DEFAULT 0 COMMENT '关注总数',
  `content_total` bigint(0) NULL DEFAULT 0 COMMENT '内容发布总数',
  `like_total` bigint(0) NULL DEFAULT 0 COMMENT '获得点赞总数',
  `collect_total` bigint(0) NULL DEFAULT 0 COMMENT '获得收藏总数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 185 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户计数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_count
-- ----------------------------
INSERT INTO `t_user_count` VALUES (103, 10, 0, 0, 11, 3, 1);

-- ----------------------------
-- Table structure for t_user_role_rel
-- ----------------------------
DROP TABLE IF EXISTS `t_user_role_rel`;
CREATE TABLE `t_user_role_rel`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` bigint(0) UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0：未删除 1：已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_role_rel
-- ----------------------------
INSERT INTO `t_user_role_rel` VALUES (8, 10, 1, '2026-01-16 13:57:42', '2026-01-16 13:57:42', b'0');

SET FOREIGN_KEY_CHECKS = 1;
