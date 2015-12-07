CREATE TABLE `faction_packs` (
  `account_id` int(11) NOT NULL,
  `receiving_player` int(11) NOT NULL,
  PRIMARY KEY (`account_id`),
  CONSTRAINT `faction_packs` FOREIGN KEY (`account_id`) REFERENCES `players` (`account_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;