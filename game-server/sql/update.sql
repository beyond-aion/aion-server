/*
 * DB changes since a70a0380 (24.11.2023)
 */

-- drop advent calendar table
drop table advent;

-- create advent calendar table
CREATE TABLE `advent` (
  `account_id` int(11) NOT NULL,
  `last_day_received` tinyint(4) NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (182007148, 186000177, 186000381, 188052986, 188052987, 188052988, 188052989, 188052990, 188052991, 188052992, 188052993, 188052994, 188052995, 188052996, 188052997, 188052998, 188052999, 188100148, 188100149);