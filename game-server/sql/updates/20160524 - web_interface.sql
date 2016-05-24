DROP TABLE IF EXISTS `web_reward`;

CREATE TABLE `web_rewards` (
	`request_id` int(11) NOT NULL DEFAULT 0,
	`receiver_name` varchar(50) NOT NULL,
	`item_id` int(11) NOT NULL DEFAULT 0,
	`item_count` int(11) NOT NULL DEFAULT 0,
	PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;