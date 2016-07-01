DROP TABLE `web_reward`;
CREATE TABLE `player_web_rewards` (
  `entry_id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `item_id` int(9) NOT NULL,
  `item_count` bigint(20) NOT NULL DEFAULT '1',
  `added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `received` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`entry_id`),
  KEY `item_owner` (`player_id`),
  CONSTRAINT `player_web_rewards_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;