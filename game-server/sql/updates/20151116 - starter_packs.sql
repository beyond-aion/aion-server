DROP TABLE IF EXISTS `starter_packs`;
CREATE TABLE `starter_packs` (
  `account_id` int(11) NOT NULL,
  `receiving_player` int(11) NOT NULL,
  PRIMARY KEY (`account_id`),
  CONSTRAINT `starter_packs` FOREIGN KEY (`account_id`) REFERENCES `players` (`account_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;