ALTER TABLE `account_data`
ADD COLUMN `allowed_hdd_serial`  varchar(100) DEFAULT NULL AFTER `last_hdd_serial`;

DROP TABLE IF EXISTS `account_login_history`;
CREATE TABLE `account_login_history` (
  `account_id` int(11) NOT NULL,
  `gameserver_id` tinyint(3) unsigned NOT NULL,
  `date` timestamp NOT NULL,
  `ip` varchar(20) DEFAULT NULL,
  `mac` varchar(20) DEFAULT NULL,
  `hdd_serial` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`account_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;