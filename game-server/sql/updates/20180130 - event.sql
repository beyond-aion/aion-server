DROP TABLE `event_ranks`;
CREATE TABLE `event` (
  `event_name` varchar(255) NOT NULL,
  `buff_index` int(11) NOT NULL,
  `buff_active_pool_ids` varchar(255) DEFAULT NULL,
  `buff_allowed_days` varchar(255) DEFAULT NULL,
  `last_change` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_name`,`buff_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `player_effects`
CHANGE COLUMN `current_time` `remaining_time`  int(11) NOT NULL AFTER `skill_lvl`,
ADD COLUMN `force_type`  varchar(255) NULL AFTER `end_time`;