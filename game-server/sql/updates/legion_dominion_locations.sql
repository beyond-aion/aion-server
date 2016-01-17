DROP TABLE IF EXISTS `legion_dominion_locations`;
CREATE TABLE `legion_dominion_locations` (
	`id` int(11) NOT NULL DEFAULT 0,
	`legion_id` int(11) NOT NULL DEFAULT 0,
	`occupied_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `legion_dominion_participants`;
CREATE TABLE `legion_dominion_participants` (
	`legion_dominion_id` int(11) NOT NULL DEFAULT 0,
	`legion_id` int(11) NOT NULL DEFAULT 0,
	`points` int(11) NOT NULL DEFAULT 0,
	`survived_time` int(11) NOT NULL DEFAULT 0,
	`participated_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`legion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;