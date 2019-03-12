DROP TABLE IF EXISTS `player_veteran_rewards`;
CREATE TABLE `player_veteran_rewards` (
  `player_id` int(11) NOT NULL,
  `received_months` tinyint(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  CONSTRAINT `player_veteran_rewards_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;