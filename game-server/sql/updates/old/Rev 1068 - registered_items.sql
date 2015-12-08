ALTER TABLE `houses` ADD UNIQUE INDEX (`address`);

CREATE TABLE `player_registered_items` (
  `player_id` int(10) NOT NULL,
  `address` int(10) NOT NULL,
  `item_unique_id` int(10) NOT NULL,
  `item_id` int(10) NOT NULL,
  `expire_time` int(20) DEFAULT NULL,
  `owner_use_count` int(10) NOT NULL DEFAULT '0',
  `visitor_use_count` int(10) NOT NULL DEFAULT '0',
  `x` float NOT NULL DEFAULT '0',
  `y` float NOT NULL DEFAULT '0',
  `z` float NOT NULL DEFAULT '0',
  `h` smallint(3) NOT NULL DEFAULT '0',
  `area` enum('NONE','INTERIOR','EXTERIOR','ALL','DECOR') NOT NULL DEFAULT 'NONE',
  PRIMARY KEY (`player_id`,`item_unique_id`,`address`,`item_id`),
  KEY `house_regitems_ibfk_1` (`address`),
  CONSTRAINT `house_regitems_ibfk_1` FOREIGN KEY (`address`) REFERENCES `houses` (`address`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `player_regitems_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

