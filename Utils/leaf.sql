/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : leaf

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 14/03/2026 16:29:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for leaf_alloc
-- ----------------------------
DROP TABLE IF EXISTS `leaf_alloc`;
CREATE TABLE `leaf_alloc`  (
  `biz_tag` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `max_id` bigint(0) NOT NULL DEFAULT 1,
  `step` int(0) NOT NULL,
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`biz_tag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of leaf_alloc
-- ----------------------------
INSERT INTO `leaf_alloc` VALUES ('leaf_segment_test', 1001001, 1000, 'segment test biz_tag', '2026-02-28 13:01:00');
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-comment-id', 1020000, 2000, '评论 ID', '2026-03-13 01:36:34');
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-communitysharing-id', 11002001, 2000, '内容分析 ID', '2026-03-05 20:03:09');
INSERT INTO `leaf_alloc` VALUES ('leaf-segment-user-id', 11000001, 1000, '用户 ID', '2026-02-28 12:56:51');

SET FOREIGN_KEY_CHECKS = 1;
