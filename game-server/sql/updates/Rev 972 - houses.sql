CREATE TABLE `houses` (
  `id` int(10) NOT NULL,
  `player_id` int(10) NOT NULL DEFAULT '0',
  `building_id` int(10) NOT NULL,
  `address` int(10) NOT NULL,
  `roof` int(10) DEFAULT NULL,
  `outwall` int(10) DEFAULT NULL,
  `inwall` int(10) DEFAULT NULL,
  `infloor` int(10) DEFAULT NULL,
  `frame` int(10) DEFAULT NULL,
  `door` int(10) DEFAULT NULL,
  `garden` int(10) DEFAULT NULL,
  `fence` int(10) DEFAULT NULL,
  `addon` int(10) DEFAULT NULL,
  `acquire_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `settings` int(11) NOT NULL DEFAULT '0',
  `status` enum('ACTIVE','SELL_WAIT','INACTIVE','NOSALE') NOT NULL DEFAULT 'ACTIVE',
  `fee_paid` tinyint(1) NOT NULL DEFAULT '1',
  `next_pay` timestamp NULL DEFAULT NULL,
  `sell_started` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`,`building_id`),
  KEY `address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `house_bids` (
  `player_id` int(10) NOT NULL,
  `house_id` int(10) NOT NULL,
  `bid` bigint(20) NOT NULL,
  `bid_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`house_id`),
  KEY `house_id_ibfk_1` (`house_id`),
  CONSTRAINT `house_id_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `player_id_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
