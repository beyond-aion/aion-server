DROP TABLE IF EXISTS `legion_dominion_locations`;
CREATE TABLE `legion_dominion_locations` (
	`id` int(11) NOT NULL DEFAULT 0,
	`legion_id` int(11) NOT NULL DEFAULT 0,
	`occupied_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `legion_dominion_locations` VALUES ('1', '0', CURRENT_TIMESTAMP);
INSERT INTO `legion_dominion_locations` VALUES ('2', '0', CURRENT_TIMESTAMP);
INSERT INTO `legion_dominion_locations` VALUES ('3', '0', CURRENT_TIMESTAMP);
INSERT INTO `legion_dominion_locations` VALUES ('4', '0', CURRENT_TIMESTAMP);
INSERT INTO `legion_dominion_locations` VALUES ('5', '0', CURRENT_TIMESTAMP);
INSERT INTO `legion_dominion_locations` VALUES ('6', '0', CURRENT_TIMESTAMP);

DROP TABLE IF EXISTS `legion_dominion_participants`;
CREATE TABLE `legion_dominion_participants` (
	`legion_dominion_id` int(11) NOT NULL DEFAULT 0,
	`legion_id` int(11) NOT NULL DEFAULT 0,
	`points` int(11) NOT NULL DEFAULT 0,
	`survived_time` int(11) NOT NULL DEFAULT 0,
	`participated_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`legion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;