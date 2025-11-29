/*
* DB changes since 937026f (28.11.2025)
 */

DROP TABLE IF EXISTS advent;
CREATE TABLE `advent` (
	`account_id` int NOT NULL,
	`last_day_received` date NOT NULL,
	PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;