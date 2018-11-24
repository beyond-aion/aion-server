DROP TABLE IF EXISTS `advent`;
CREATE TABLE `advent` (
  `account_id` int(11) NOT NULL,
  `last_day_received` tinyint(4) NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;