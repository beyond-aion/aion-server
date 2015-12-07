CREATE TABLE `house_object_cooldowns` (
  `player_id` int(11) NOT NULL,
  `template_id` int(11) NOT NULL,
  `reuse_time` bigint(20) NOT NULL,
  PRIMARY KEY (`player_id`,`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;