DROP TABLE IF EXISTS `commands_access`;

CREATE TABLE `commands_access` (
  `player_id` int(11) NOT NULL,
  `command` varchar(40) NOT NULL,
  PRIMARY KEY (`player_id`,`command`),
  CONSTRAINT `commands_access_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8