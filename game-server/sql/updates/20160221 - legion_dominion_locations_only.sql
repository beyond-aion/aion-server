-- ----------------------------
-- Table structure for `legion_dominion_locations`
-- ----------------------------
DROP TABLE IF EXISTS `legion_dominion_locations`;
CREATE TABLE `legion_dominion_locations` (
  `id` int(11) NOT NULL DEFAULT '0',
  `legion_id` int(11) NOT NULL DEFAULT '0',
  `occupied_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of legion_dominion_locations
-- ----------------------------
INSERT INTO `legion_dominion_locations` VALUES ('1', '0', '2016-02-21 03:51:41');
INSERT INTO `legion_dominion_locations` VALUES ('2', '0', '2016-02-21 03:51:41');
INSERT INTO `legion_dominion_locations` VALUES ('3', '0', '2016-02-21 03:51:41');
INSERT INTO `legion_dominion_locations` VALUES ('4', '0', '2016-02-21 03:51:41');
INSERT INTO `legion_dominion_locations` VALUES ('5', '0', '2016-02-21 03:51:41');
INSERT INTO `legion_dominion_locations` VALUES ('6', '0', '2016-02-21 03:51:41');
