DROP TABLE IF EXISTS `custom_instance`;
CREATE TABLE `custom_instance` (
  `player_id` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  `last_entry` timestamp NOT NULL,
  PRIMARY KEY (`player_id`),
  CONSTRAINT `custom_instance_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `custom_instance_records`;
CREATE TABLE `custom_instance_records` (
  `player_id`  int NOT NULL,
  `timestamp`  TIMESTAMP NOT NULL,
  `skill_id`  int NOT NULL,
  `player_class_id`  int NOT NULL,
  `player_hp_percentage`  float NOT NULL,
  `player_mp_percentage`  float NOT NULL,
  `player_is_rooted`  tinyint(1) NOT NULL,
  `player_is_silenced`  tinyint(1) NOT NULL,
  `player_is_bound`  tinyint(1) NOT NULL,
  `player_is_stunned`  tinyint(1) NOT NULL,
  `player_is_aetherhold`  tinyint(1) NOT NULL,
  `player_buff_count`  int NOT NULL,
  `player_debuff_count`  int NOT NULL,
  `player_is_shielded`  tinyint(1) NOT NULL,
  `target_hp_percentage`  float NULL,
  `target_mp_percentage`  float NULL,
  `target_focuses_player`  tinyint(1) NULL,
  `distance`  float NULL,
  `target_is_rooted`  tinyint(1) NULL,
  `target_is_silenced`  tinyint(1) NULL,
  `target_is_bound`  tinyint(1) NULL,
  `target_is_stunned`  tinyint(1) NULL,
  `target_is_aetherhold`  tinyint(1) NULL,
  `target_buff_count`  int NULL,
  `target_debuff_count`  int NULL,
  `target_is_shielded`  tinyint(1) NULL,
  CONSTRAINT `custom_instance_records_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;