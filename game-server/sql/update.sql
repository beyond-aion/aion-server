/*
 * DB changes since ec7c95de (14.11.2021)
 */

-- create advent calendar table
CREATE TABLE `advent` (
  `account_id` int(11) NOT NULL,
  `last_day_received` tinyint(4) NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;