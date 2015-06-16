CREATE TABLE `craft_cooldowns` (
  `player_id` int(11) unsigned NOT NULL,
  `delay_id` int(11) unsigned NOT NULL,
  `reuse_time` bigint(13) unsigned NOT NULL,
  PRIMARY KEY (`player_id`,`delay_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;