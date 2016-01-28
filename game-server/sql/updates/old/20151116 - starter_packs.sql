DROP TABLE IF EXISTS `starter_packs`;
CREATE TABLE `starter_packs` (
  `account_id` int(11) NOT NULL,
  `receiving_player` int(11) NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;