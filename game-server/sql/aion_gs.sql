SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `abyss_rank`
-- ----------------------------
DROP TABLE IF EXISTS `abyss_rank`;
CREATE TABLE `abyss_rank` (
  `player_id` int NOT NULL,
  `daily_ap` int NOT NULL,
  `weekly_ap` int NOT NULL,
  `ap` int NOT NULL,
  `rank` tinyint NOT NULL DEFAULT '1',
  `max_rank` tinyint NOT NULL DEFAULT '1',
  `rank_pos` smallint NOT NULL DEFAULT '0',
  `old_rank_pos` smallint NOT NULL DEFAULT '0',
  `daily_kill` int NOT NULL,
  `weekly_kill` int NOT NULL,
  `all_kill` int NOT NULL DEFAULT '0',
  `last_kill` int NOT NULL,
  `last_ap` int NOT NULL,
  `last_update` decimal(20, 0) NOT NULL,
  `rank_ap` int NOT NULL DEFAULT '0',
  `daily_gp` int NOT NULL DEFAULT '0',
  `weekly_gp` int NOT NULL DEFAULT '0',
  `gp` int NOT NULL DEFAULT '0',
  `last_gp` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  KEY `rank` (`rank`),
  KEY `rank_pos` (`rank_pos`),
  KEY `gp` (`gp`),
  CONSTRAINT `abyss_rank_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `account_passports`
-- ----------------------------
DROP TABLE IF EXISTS `account_passports`;
CREATE TABLE `account_passports` (
  `account_id` int NOT NULL,
  `passport_id` int NOT NULL,
  `rewarded` int NOT NULL,
  `arrive_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`,`passport_id`,`arrive_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `account_stamps`
-- ----------------------------
DROP TABLE IF EXISTS `account_stamps`;
CREATE TABLE `account_stamps` (
  `account_id` int NOT NULL,
  `stamps` tinyint NOT NULL DEFAULT '0',
  `last_stamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `advent`
-- ----------------------------
DROP TABLE IF EXISTS `advent`;
CREATE TABLE `advent` (
  `account_id` int NOT NULL,
  `last_day_received` date NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `announcements`
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `announce` text NOT NULL,
  `faction` enum('ALL','ASMODIANS','ELYOS') NOT NULL DEFAULT 'ALL',
  `type` enum('SHOUT','ORANGE','YELLOW','WHITE','SYSTEM') NOT NULL DEFAULT 'SYSTEM',
  `delay` int NOT NULL DEFAULT '1800',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `blocks`
-- ----------------------------
DROP TABLE IF EXISTS `blocks`;
CREATE TABLE `blocks` (
  `player` int NOT NULL,
  `blocked_player` int NOT NULL,
  `reason` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`player`,`blocked_player`),
  KEY `blocked_player` (`blocked_player`),
  CONSTRAINT `blocks_ibfk_1` FOREIGN KEY (`player`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `blocks_ibfk_2` FOREIGN KEY (`blocked_player`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `bonus_packs`
-- ----------------------------
DROP TABLE IF EXISTS `bonus_packs`;
CREATE TABLE `bonus_packs` (
  `account_id` int NOT NULL,
  `receiving_player` int NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `bookmark`
-- ----------------------------
DROP TABLE IF EXISTS `bookmark`;
CREATE TABLE `bookmark` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `char_id` int NOT NULL,
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `world_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `broker`
-- ----------------------------
DROP TABLE IF EXISTS `broker`;
CREATE TABLE `broker` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_pointer` int NOT NULL DEFAULT '0',
  `item_id` int NOT NULL,
  `item_count` bigint NOT NULL,
  `item_creator` varchar(50) DEFAULT NULL,
  `price` bigint NOT NULL DEFAULT '0',
  `broker_race` enum('ELYOS','ASMODIAN') NOT NULL,
  `expire_time` timestamp NOT NULL DEFAULT '2010-01-01 02:00:00',
  `settle_time` timestamp NOT NULL DEFAULT '2010-01-01 02:00:00',
  `seller_id` int NOT NULL,
  `is_sold` boolean NOT NULL,
  `is_settled` boolean NOT NULL,
  `splitting_available` boolean NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `seller_id` (`seller_id`),
  CONSTRAINT `broker_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `challenge_tasks`
-- ----------------------------
DROP TABLE IF EXISTS `challenge_tasks`;
CREATE TABLE `challenge_tasks` (
  `task_id` int NOT NULL,
  `quest_id` int NOT NULL,
  `owner_id` int NOT NULL,
  `owner_type` enum('LEGION','TOWN') NOT NULL,
  `complete_count` int unsigned NOT NULL DEFAULT '0',
  `complete_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`task_id`,`quest_id`,`owner_id`,`owner_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `commands_access`
-- ----------------------------
DROP TABLE IF EXISTS `commands_access`;
CREATE TABLE `commands_access` (
  `player_id` int NOT NULL,
  `command` varchar(40) NOT NULL,
  PRIMARY KEY (`player_id`,`command`),
  CONSTRAINT `commands_access_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `craft_cooldowns`
-- ----------------------------
DROP TABLE IF EXISTS `craft_cooldowns`;
CREATE TABLE `craft_cooldowns` (
  `player_id` int NOT NULL,
  `delay_id` int unsigned NOT NULL,
  `reuse_time` bigint unsigned NOT NULL,
  PRIMARY KEY (`player_id`,`delay_id`),
  CONSTRAINT `craft_cooldowns_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `custom_instance`
-- ----------------------------
DROP TABLE IF EXISTS `custom_instance`;
CREATE TABLE `custom_instance` (
  `player_id` int NOT NULL,
  `rank` int NOT NULL,
  `last_entry` timestamp NOT NULL,
  `max_rank` int NOT NULL,
  `dps` int NOT NULL,
  PRIMARY KEY (`player_id`),
  KEY `rank` (`rank`),
  KEY `last_entry` (`last_entry`),
  CONSTRAINT `custom_instance_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `custom_instance_records`
-- ----------------------------
DROP TABLE IF EXISTS `custom_instance_records`;
CREATE TABLE `custom_instance_records` (
  `player_id`  int NOT NULL,
  `timestamp`  TIMESTAMP NOT NULL,
  `skill_id`  int NOT NULL,
  `player_class_id`  int NOT NULL,
  `player_hp_percentage`  float NOT NULL,
  `player_mp_percentage`  float NOT NULL,
  `player_is_rooted`  boolean NOT NULL,
  `player_is_silenced`  boolean NOT NULL,
  `player_is_bound`  boolean NOT NULL,
  `player_is_stunned`  boolean NOT NULL,
  `player_is_aetherhold`  boolean NOT NULL,
  `player_buff_count`  int NOT NULL,
  `player_debuff_count`  int NOT NULL,
  `player_is_shielded`  boolean NOT NULL,
  `target_hp_percentage`  float NULL,
  `target_mp_percentage`  float NULL,
  `target_focuses_player`  boolean NULL,
  `distance`  float NULL,
  `target_is_rooted`  boolean NULL,
  `target_is_silenced`  boolean NULL,
  `target_is_bound`  boolean NULL,
  `target_is_stunned`  boolean NULL,
  `target_is_aetherhold`  boolean NULL,
  `target_buff_count`  int NULL,
  `target_debuff_count`  int NULL,
  `target_is_shielded`  boolean NULL,
  CONSTRAINT `custom_instance_records_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `event`
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `event_name` varchar(255) NOT NULL,
  `buff_index` int NOT NULL,
  `buff_active_pool_ids` varchar(255) DEFAULT NULL,
  `buff_allowed_days` varchar(255) DEFAULT NULL,
  `last_change` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_name`,`buff_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `faction_packs`
-- ----------------------------
DROP TABLE IF EXISTS `faction_packs`;
CREATE TABLE `faction_packs` (
  `account_id` int NOT NULL,
  `receiving_player` int NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `friends`
-- ----------------------------
DROP TABLE IF EXISTS `friends`;
CREATE TABLE `friends` (
  `player` int NOT NULL,
  `friend` int NOT NULL,
  `memo` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`player`,`friend`),
  KEY `friend` (`friend`),
  CONSTRAINT `friends_ibfk_1` FOREIGN KEY (`player`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `friends_ibfk_2` FOREIGN KEY (`friend`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `guides`
-- ----------------------------
DROP TABLE IF EXISTS `guides`;
CREATE TABLE `guides` (
  `guide_id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `title` varchar(80) NOT NULL,
  PRIMARY KEY (`guide_id`),
  KEY `player_id` (`player_id`),
  CONSTRAINT `guides_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `headhunting`
-- ----------------------------
DROP TABLE IF EXISTS `headhunting`;
CREATE TABLE `headhunting` (
  `hunter_id` int NOT NULL,
  `accumulated_kills` int NOT NULL,
  `last_update` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`hunter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `house_bids`
-- ----------------------------
DROP TABLE IF EXISTS `house_bids`;
CREATE TABLE `house_bids` (
  `player_id` int NOT NULL,
  `house_id` int NOT NULL,
  `bid` bigint NOT NULL,
  `bid_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`house_id`,`bid`),
  KEY `house_id_ibfk_1` (`house_id`),
  CONSTRAINT `house_id_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `house_object_cooldowns`
-- ----------------------------
DROP TABLE IF EXISTS `house_object_cooldowns`;
CREATE TABLE `house_object_cooldowns` (
  `player_id` int NOT NULL,
  `object_id` int NOT NULL,
  `reuse_time` bigint NOT NULL,
  PRIMARY KEY (`player_id`,`object_id`),
  CONSTRAINT `house_object_cooldowns_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `house_object_cooldowns_ibfk_2` FOREIGN KEY (`object_id`) REFERENCES `player_registered_items` (`item_unique_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `house_scripts`
-- ----------------------------
DROP TABLE IF EXISTS `house_scripts`;
CREATE TABLE `house_scripts` (
  `house_id` int NOT NULL,
  `script_id` tinyint NOT NULL,
  `script` mediumtext NOT NULL,
  `date_added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`house_id`,`script_id`),
  CONSTRAINT `houses_id_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=16;

-- ----------------------------
-- Table structure for `houses`
-- ----------------------------
DROP TABLE IF EXISTS `houses`;
CREATE TABLE `houses` (
  `id` int NOT NULL,
  `player_id` int NOT NULL DEFAULT '0',
  `building_id` int NOT NULL,
  `address` int NOT NULL,
  `acquire_time` timestamp NULL DEFAULT NULL,
  `settings` int NOT NULL DEFAULT '0',
  `next_pay` timestamp NULL DEFAULT NULL,
  `sign_notice` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `ingameshop`
-- ----------------------------
DROP TABLE IF EXISTS `ingameshop`;
CREATE TABLE `ingameshop` (
  `object_id` int NOT NULL AUTO_INCREMENT,
  `item_id` int NOT NULL,
  `item_count` bigint NOT NULL DEFAULT '0',
  `item_price` bigint NOT NULL DEFAULT '0',
  `category` tinyint NOT NULL DEFAULT '0',
  `sub_category` tinyint NOT NULL DEFAULT '0',
  `list` int NOT NULL DEFAULT '0',
  `sales_ranking` int NOT NULL DEFAULT '0',
  `item_type` tinyint NOT NULL DEFAULT '0',
  `gift` boolean NOT NULL DEFAULT '0',
  `title_description` varchar(20) NOT NULL,
  `description` varchar(20) NOT NULL,
  PRIMARY KEY (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `ingameshop_log`
-- ----------------------------
DROP TABLE IF EXISTS `ingameshop_log`;
CREATE TABLE `ingameshop_log` (
  `transaction_id` int NOT NULL AUTO_INCREMENT,
  `transaction_type` enum('BUY','GIFT') NOT NULL,
  `transaction_date` timestamp NULL DEFAULT NULL,
  `payer_name` varchar(50) NOT NULL,
  `payer_account_name` varchar(50) NOT NULL,
  `receiver_name` varchar(50) NOT NULL,
  `item_id` int NOT NULL,
  `item_count` bigint NOT NULL DEFAULT '0',
  `item_price` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `inventory`
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `item_unique_id` int NOT NULL,
  `item_id` int NOT NULL,
  `item_count` bigint NOT NULL DEFAULT '0',
  `item_color` mediumint unsigned DEFAULT NULL,
  `color_expires` int NOT NULL DEFAULT '0',
  `item_creator` varchar(50) DEFAULT NULL,
  `expire_time` int NOT NULL DEFAULT '0',
  `activation_count` int NOT NULL DEFAULT '0',
  `item_owner` int NOT NULL,
  `is_equipped` boolean NOT NULL DEFAULT '0',
  `is_soul_bound` boolean NOT NULL DEFAULT '0',
  `slot` bigint NOT NULL DEFAULT '0',
  `item_location` tinyint DEFAULT '0',
  `enchant` tinyint unsigned NOT NULL DEFAULT '0',
  `enchant_bonus` tinyint NOT NULL DEFAULT '0',
  `item_skin` int NOT NULL DEFAULT '0',
  `fusioned_item` int NOT NULL DEFAULT '0',
  `optional_socket` tinyint unsigned NOT NULL DEFAULT '0',
  `optional_fusion_socket` tinyint unsigned NOT NULL DEFAULT '0',
  `charge` mediumint NOT NULL DEFAULT '0',
  `tune_count` smallint NOT NULL DEFAULT '0',
  `rnd_bonus` smallint NOT NULL DEFAULT '0',
  `fusion_rnd_bonus` smallint NOT NULL DEFAULT '0',
  `tempering` tinyint unsigned NOT NULL DEFAULT '0',
  `pack_count` smallint NOT NULL DEFAULT '0',
  `is_amplified` boolean NOT NULL DEFAULT '0',
  `buff_skill` int NOT NULL DEFAULT '0',
  `rnd_plume_bonus` smallint NOT NULL DEFAULT '0',
  PRIMARY KEY (`item_unique_id`),
  KEY `item_location` (`item_location`),
  KEY `index3` (`item_owner`,`item_location`,`is_equipped`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `item_cooldowns`
-- ----------------------------
DROP TABLE IF EXISTS `item_cooldowns`;
CREATE TABLE `item_cooldowns` (
  `player_id` int NOT NULL,
  `delay_id` int NOT NULL,
  `use_delay` int unsigned NOT NULL,
  `reuse_time` bigint NOT NULL,
  PRIMARY KEY (`player_id`,`delay_id`),
  CONSTRAINT `item_cooldowns_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `item_stones`
-- ----------------------------
DROP TABLE IF EXISTS `item_stones`;
CREATE TABLE `item_stones` (
  `item_unique_id` int NOT NULL,
  `item_id` int NOT NULL,
  `slot` int NOT NULL,
  `category` int NOT NULL DEFAULT '0',
  `polishNumber` int NOT NULL,
  `polishCharge` int NOT NULL,
  `proc_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`item_unique_id`,`slot`,`category`),
  CONSTRAINT `item_stones_ibfk_1` FOREIGN KEY (`item_unique_id`) REFERENCES `inventory` (`item_unique_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legion_announcement_list`
-- ----------------------------
DROP TABLE IF EXISTS `legion_announcement_list`;
CREATE TABLE `legion_announcement_list` (
  `legion_id` int NOT NULL,
  `announcement` varchar(256) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `legion_id` (`legion_id`),
  CONSTRAINT `legion_announcement_list_ibfk_1` FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legion_dominion_locations`
-- ----------------------------
DROP TABLE IF EXISTS `legion_dominion_locations`;
CREATE TABLE `legion_dominion_locations` (
  `id` int NOT NULL DEFAULT '0',
  `legion_id` int NOT NULL DEFAULT '0',
  `occupied_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legion_dominion_participants`
-- ----------------------------
DROP TABLE IF EXISTS `legion_dominion_participants`;
CREATE TABLE `legion_dominion_participants` (
  `legion_dominion_id` int NOT NULL DEFAULT '0',
  `legion_id` int NOT NULL DEFAULT '0',
  `points` int NOT NULL DEFAULT '0',
  `survived_time` int NOT NULL DEFAULT '0',
  `participated_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`legion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legion_emblems`
-- ----------------------------
DROP TABLE IF EXISTS `legion_emblems`;
CREATE TABLE `legion_emblems` (
  `legion_id` int NOT NULL,
  `emblem_id` tinyint NOT NULL DEFAULT '0',
  `color_a` tinyint NOT NULL DEFAULT '0',
  `color_r` tinyint NOT NULL DEFAULT '0',
  `color_g` tinyint NOT NULL DEFAULT '0',
  `color_b` tinyint NOT NULL DEFAULT '0',
  `emblem_type` enum('DEFAULT','CUSTOM') NOT NULL DEFAULT 'DEFAULT',
  `emblem_data` longblob,
  PRIMARY KEY (`legion_id`),
  CONSTRAINT `legion_emblems_ibfk_1` FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legion_history`
-- ----------------------------
DROP TABLE IF EXISTS `legion_history`;
CREATE TABLE `legion_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `legion_id` int NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `history_type` enum('CREATE','JOIN','KICK','APPOINTED','EMBLEM_REGISTER','EMBLEM_MODIFIED','ITEM_DEPOSIT','ITEM_WITHDRAW','KINAH_DEPOSIT','KINAH_WITHDRAW','LEVEL_UP','DEFENSE','OCCUPATION','LEGION_RENAME','CHARACTER_RENAME') NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(30) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `legion_id` (`legion_id`),
  CONSTRAINT `legion_history_ibfk_1` FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legion_members`
-- ----------------------------
DROP TABLE IF EXISTS `legion_members`;
CREATE TABLE `legion_members` (
  `legion_id` int NOT NULL,
  `player_id` int NOT NULL,
  `nickname` varchar(10) NOT NULL DEFAULT '',
  `rank` enum('BRIGADE_GENERAL','CENTURION','LEGIONARY','DEPUTY','VOLUNTEER') NOT NULL DEFAULT 'VOLUNTEER',
  `selfintro` varchar(32) DEFAULT '',
  `challenge_score` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  KEY `player_id` (`player_id`),
  KEY `legion_id` (`legion_id`),
  CONSTRAINT `legion_members_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `legion_members_ibfk_2` FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `legions`
-- ----------------------------
DROP TABLE IF EXISTS `legions`;
CREATE TABLE `legions` (
  `id` int NOT NULL,
  `name` varchar(32) NOT NULL,
  `level` int NOT NULL DEFAULT '1',
  `contribution_points` bigint NOT NULL DEFAULT '0',
  `deputy_permission` int NOT NULL DEFAULT '7692',
  `centurion_permission` int NOT NULL DEFAULT '7176',
  `legionary_permission` int NOT NULL DEFAULT '6144',
  `volunteer_permission` int NOT NULL DEFAULT '2048',
  `disband_time` int NOT NULL DEFAULT '0',
  `rank_pos` smallint NOT NULL DEFAULT '0',
  `old_rank_pos` smallint NOT NULL DEFAULT '0',
  `occupied_legion_dominion` int NOT NULL DEFAULT '0',
  `last_legion_dominion` int NOT NULL DEFAULT '0',
  `current_legion_dominion` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`),
  KEY `rank_pos` (`rank_pos`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `mail`
-- ----------------------------
DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `mail_unique_id` int NOT NULL,
  `mail_recipient_id` int NOT NULL,
  `sender_name` varchar(20) NOT NULL,
  `mail_title` varchar(20) NOT NULL,
  `mail_message` varchar(1000) NOT NULL,
  `unread` boolean NOT NULL DEFAULT '1',
  `attached_item_id` int NOT NULL,
  `attached_kinah_count` bigint NOT NULL,
  `express` tinyint NOT NULL DEFAULT '0',
  `recieved_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`mail_unique_id`),
  KEY `mail_recipient_id` (`mail_recipient_id`),
  CONSTRAINT `FK_mail` FOREIGN KEY (`mail_recipient_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `old_names`
-- ----------------------------
DROP TABLE IF EXISTS `old_names`;
CREATE TABLE `old_names` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `old_name` varchar(50) NOT NULL,
  `new_name` varchar(50) NOT NULL,
  `renamed_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `player_id` (`player_id`),
  KEY `renamed_date` (`renamed_date`),
  CONSTRAINT `old_names_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_appearance`
-- ----------------------------
DROP TABLE IF EXISTS `player_appearance`;
CREATE TABLE `player_appearance` (
  `player_id` int NOT NULL,
  `face` int NOT NULL,
  `hair` int NOT NULL,
  `deco` int NOT NULL,
  `tattoo` int NOT NULL,
  `face_contour` int NOT NULL,
  `expression` int NOT NULL,
  `jaw_line` int NOT NULL,
  `skin_rgb` int NOT NULL,
  `hair_rgb` int NOT NULL,
  `lip_rgb` int NOT NULL,
  `eye_rgb` int NOT NULL,
  `face_shape` int NOT NULL,
  `forehead` int NOT NULL,
  `eye_height` int NOT NULL,
  `eye_space` int NOT NULL,
  `eye_width` int NOT NULL,
  `eye_size` int NOT NULL,
  `eye_shape` int NOT NULL,
  `eye_angle` int NOT NULL,
  `brow_height` int NOT NULL,
  `brow_angle` int NOT NULL,
  `brow_shape` int NOT NULL,
  `nose` int NOT NULL,
  `nose_bridge` int NOT NULL,
  `nose_width` int NOT NULL,
  `nose_tip` int NOT NULL,
  `cheek` int NOT NULL,
  `lip_height` int NOT NULL,
  `mouth_size` int NOT NULL,
  `lip_size` int NOT NULL,
  `smile` int NOT NULL,
  `lip_shape` int NOT NULL,
  `jaw_height` int NOT NULL,
  `chin_jut` int NOT NULL,
  `ear_shape` int NOT NULL,
  `head_size` int NOT NULL,
  `neck` int NOT NULL,
  `neck_length` int NOT NULL,
  `shoulders` int NOT NULL,
  `shoulder_size` int NOT NULL,
  `torso` int NOT NULL,
  `chest` int NOT NULL,
  `waist` int NOT NULL,
  `hips` int NOT NULL,
  `arm_thickness` int NOT NULL,
  `arm_length` int NOT NULL,
  `hand_size` int NOT NULL,
  `leg_thickness` int NOT NULL,
  `leg_length` int NOT NULL,
  `foot_size` int NOT NULL,
  `facial_rate` int NOT NULL,
  `voice` int NOT NULL,
  `height` float NOT NULL,
  PRIMARY KEY (`player_id`),
  CONSTRAINT `player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_bind_point`
-- ----------------------------
DROP TABLE IF EXISTS `player_bind_point`;
CREATE TABLE `player_bind_point` (
  `player_id` int NOT NULL,
  `map_id` int NOT NULL,
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `heading` int NOT NULL,
  PRIMARY KEY (`player_id`),
  CONSTRAINT `player_bind_point_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_cooldowns`
-- ----------------------------
DROP TABLE IF EXISTS `player_cooldowns`;
CREATE TABLE `player_cooldowns` (
  `player_id` int NOT NULL,
  `cooldown_id` int NOT NULL,
  `reuse_delay` bigint NOT NULL,
  PRIMARY KEY (`player_id`,`cooldown_id`),
  CONSTRAINT `player_cooldowns_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_effects`
-- ----------------------------
DROP TABLE IF EXISTS `player_effects`;
CREATE TABLE `player_effects` (
  `player_id` int NOT NULL,
  `skill_id` int NOT NULL,
  `skill_lvl` tinyint NOT NULL,
  `remaining_time` int NOT NULL,
  `end_time` bigint NOT NULL,
  `force_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`player_id`,`skill_id`),
  CONSTRAINT `player_effects_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_emotions`
-- ----------------------------
DROP TABLE IF EXISTS `player_emotions`;
CREATE TABLE `player_emotions` (
  `player_id` int NOT NULL,
  `emotion` int NOT NULL,
  `remaining` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`emotion`),
  CONSTRAINT `player_emotions_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_life_stats`
-- ----------------------------
DROP TABLE IF EXISTS `player_life_stats`;
CREATE TABLE `player_life_stats` (
  `player_id` int NOT NULL,
  `hp` int NOT NULL DEFAULT '1',
  `mp` int NOT NULL DEFAULT '1',
  `fp` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`player_id`),
  CONSTRAINT `FK_player_life_stats` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_macrosses`
-- ----------------------------
DROP TABLE IF EXISTS `player_macrosses`;
CREATE TABLE `player_macrosses` (
  `player_id` int NOT NULL,
  `order` int NOT NULL,
  `macro` text NOT NULL,
  UNIQUE KEY `main` (`player_id`,`order`),
  CONSTRAINT `player_macrosses_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_motions`
-- ----------------------------
DROP TABLE IF EXISTS `player_motions`;
CREATE TABLE `player_motions` (
  `player_id` int NOT NULL,
  `motion_id` int NOT NULL,
  `time` int NOT NULL DEFAULT '0',
  `active` boolean NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`motion_id`) USING BTREE,
  CONSTRAINT `motions_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_npc_factions`
-- ----------------------------
DROP TABLE IF EXISTS `player_npc_factions`;
CREATE TABLE `player_npc_factions` (
  `player_id` int NOT NULL,
  `faction_id` int NOT NULL,
  `active` boolean NOT NULL,
  `time` int NOT NULL,
  `state` enum('NOTING','START','COMPLETE') NOT NULL DEFAULT 'NOTING',
  `quest_id` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`faction_id`),
  CONSTRAINT `player_npc_factions_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_passkey`
-- ----------------------------
DROP TABLE IF EXISTS `player_passkey`;
CREATE TABLE `player_passkey` (
  `account_id` int NOT NULL,
  `passkey` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`account_id`,`passkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_pets`
-- ----------------------------
DROP TABLE IF EXISTS `player_pets`;
CREATE TABLE `player_pets` (
  `id` int NOT NULL,
  `player_id` int NOT NULL,
  `template_id` int NOT NULL,
  `decoration` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `hungry_level` tinyint NOT NULL DEFAULT '0',
  `feed_progress` int NOT NULL DEFAULT '0',
  `reuse_time` bigint NOT NULL DEFAULT '0',
  `birthday` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mood_started` bigint NOT NULL DEFAULT '0',
  `counter` int NOT NULL DEFAULT '0',
  `mood_cd_started` bigint NOT NULL DEFAULT '0',
  `gift_cd_started` bigint NOT NULL DEFAULT '0',
  `dopings` varchar(80) CHARACTER SET ascii DEFAULT NULL,
  `despawn_time` timestamp NULL DEFAULT NULL,
  `expire_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `player_id` (`player_id`),
  CONSTRAINT `FK_player_pets` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_punishments`
-- ----------------------------
DROP TABLE IF EXISTS `player_punishments`;
CREATE TABLE `player_punishments` (
  `player_id` int NOT NULL,
  `punishment_type` enum('PRISON','GATHER','CHARBAN') NOT NULL,
  `start_time` int unsigned DEFAULT '0',
  `duration` int unsigned DEFAULT '0',
  `reason` text,
  PRIMARY KEY (`player_id`,`punishment_type`),
  CONSTRAINT `player_punishments_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_quests`
-- ----------------------------
DROP TABLE IF EXISTS `player_quests`;
CREATE TABLE `player_quests` (
  `player_id` int NOT NULL,
  `quest_id` int unsigned NOT NULL DEFAULT '0',
  `status` enum('LOCKED','START','REWARD','COMPLETE') NOT NULL,
  `quest_vars` int unsigned NOT NULL DEFAULT '0',
  `flags` int unsigned NOT NULL DEFAULT '0',
  `complete_count` int unsigned NOT NULL DEFAULT '0',
  `next_repeat_time` timestamp NULL DEFAULT NULL,
  `reward` smallint DEFAULT NULL,
  `complete_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`player_id`,`quest_id`),
  CONSTRAINT `player_quests_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_recipes`
-- ----------------------------
DROP TABLE IF EXISTS `player_recipes`;
CREATE TABLE `player_recipes` (
  `player_id` int NOT NULL,
  `recipe_id` int NOT NULL,
  PRIMARY KEY (`player_id`,`recipe_id`),
  CONSTRAINT `player_recipes_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_registered_items`
-- ----------------------------
DROP TABLE IF EXISTS `player_registered_items`;
CREATE TABLE `player_registered_items` (
  `player_id` int NOT NULL,
  `item_unique_id` int NOT NULL,
  `item_id` int NOT NULL,
  `expire_time` int DEFAULT NULL,
  `color` int DEFAULT NULL,
  `color_expires` int NOT NULL DEFAULT '0',
  `owner_use_count` int NOT NULL DEFAULT '0',
  `visitor_use_count` int NOT NULL DEFAULT '0',
  `x` float NOT NULL DEFAULT '0',
  `y` float NOT NULL DEFAULT '0',
  `z` float NOT NULL DEFAULT '0',
  `h` smallint DEFAULT NULL,
  `area` enum('NONE','INTERIOR','EXTERIOR','ALL','DECOR') NOT NULL DEFAULT 'NONE',
  `room` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`item_unique_id`,`item_id`),
  UNIQUE KEY `item_unique_id` (`item_unique_id`),
  CONSTRAINT `player_regitems_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_settings`
-- ----------------------------
DROP TABLE IF EXISTS `player_settings`;
CREATE TABLE `player_settings` (
  `player_id` int NOT NULL,
  `settings_type` tinyint NOT NULL,
  `settings` blob NOT NULL,
  PRIMARY KEY (`player_id`,`settings_type`),
  CONSTRAINT `ps_pl_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_skills`
-- ----------------------------
DROP TABLE IF EXISTS `player_skills`;
CREATE TABLE `player_skills` (
  `player_id` int NOT NULL,
  `skill_id` int NOT NULL,
  `skill_level` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`player_id`,`skill_id`),
  CONSTRAINT `player_skills_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_titles`
-- ----------------------------
DROP TABLE IF EXISTS `player_titles`;
CREATE TABLE `player_titles` (
  `player_id` int NOT NULL,
  `title_id` int NOT NULL,
  `remaining` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`title_id`),
  CONSTRAINT `player_titles_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_veteran_rewards`
-- ----------------------------
DROP TABLE IF EXISTS `player_veteran_rewards`;
CREATE TABLE `player_veteran_rewards` (
  `player_id` int NOT NULL,
  `received_months` tinyint unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  CONSTRAINT `player_veteran_rewards_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `player_web_rewards`
-- ----------------------------
DROP TABLE IF EXISTS `player_web_rewards`;
CREATE TABLE `player_web_rewards` (
  `entry_id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `item_id` int NOT NULL,
  `item_count` bigint NOT NULL DEFAULT '1',
  `added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `received` timestamp NULL DEFAULT NULL,
  `order_id` varchar(10) NULL DEFAULT NULL,
  PRIMARY KEY (`entry_id`),
  KEY `item_owner` (`player_id`),
  UNIQUE (`order_id`),
  CONSTRAINT `player_web_rewards_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `players`
-- ----------------------------
DROP TABLE IF EXISTS `players`;
CREATE TABLE `players` (
  `id` int NOT NULL,
  `name` varchar(50) NOT NULL,
  `account_id` int NOT NULL,
  `account_name` varchar(50) NOT NULL,
  `exp` bigint NOT NULL DEFAULT '0',
  `recoverexp` bigint NOT NULL DEFAULT '0',
  `old_level` tinyint NOT NULL DEFAULT '0',
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `heading` int NOT NULL,
  `world_id` int NOT NULL,
  `world_owner` int NOT NULL DEFAULT '0',
  `gender` enum('MALE','FEMALE') NOT NULL,
  `race` enum('ASMODIANS','ELYOS') NOT NULL,
  `player_class` enum('WARRIOR','GLADIATOR','TEMPLAR','SCOUT','ASSASSIN','RANGER','MAGE','SORCERER','SPIRIT_MASTER','PRIEST','CLERIC','CHANTER','ENGINEER','GUNNER','ARTIST','BARD','RIDER','ALL') NOT NULL,
  `creation_date` timestamp NULL DEFAULT NULL,
  `deletion_date` timestamp NULL DEFAULT NULL,
  `last_online` timestamp NULL DEFAULT NULL,
  `quest_expands` tinyint NOT NULL DEFAULT '0',
  `npc_expands` tinyint NOT NULL DEFAULT '0',
  `item_expands` tinyint NOT NULL DEFAULT '0',
  `wh_npc_expands` tinyint NOT NULL DEFAULT '0',
  `wh_bonus_expands` tinyint NOT NULL DEFAULT '0',
  `mailbox_letters` tinyint unsigned NOT NULL DEFAULT '0',
  `title_id` int NOT NULL DEFAULT '-1',
  `bonus_title_id` int NOT NULL DEFAULT '-1',
  `dp` int NOT NULL DEFAULT '0',
  `soul_sickness` tinyint unsigned NOT NULL DEFAULT '0',
  `reposte_energy` bigint NOT NULL DEFAULT '0',
  `online` boolean NOT NULL DEFAULT '0',
  `note` text,
  `mentor_flag_time` int NOT NULL DEFAULT '0',
  `last_transfer_time` decimal(20,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`),
  KEY `account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `portal_cooldowns`
-- ----------------------------
DROP TABLE IF EXISTS `portal_cooldowns`;
CREATE TABLE `portal_cooldowns` (
  `player_id` int NOT NULL,
  `world_id` int NOT NULL,
  `reuse_time` bigint NOT NULL,
  `entry_count` int NOT NULL,
  PRIMARY KEY (`player_id`,`world_id`),
  CONSTRAINT `portal_cooldowns_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `server_variables`
-- ----------------------------
DROP TABLE IF EXISTS `server_variables`;
CREATE TABLE `server_variables` (
  `key` varchar(30) NOT NULL,
  `value` varchar(30) NOT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `siege_locations`
-- ----------------------------
DROP TABLE IF EXISTS `siege_locations`;
CREATE TABLE `siege_locations` (
  `id` int NOT NULL,
  `race` enum('ELYOS','ASMODIANS','BALAUR') NOT NULL,
  `legion_id` int NOT NULL,
  `occupy_count` tinyint NOT NULL DEFAULT '0',
  `faction_balance` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `surveys`
-- ----------------------------
DROP TABLE IF EXISTS `surveys`;
CREATE TABLE `surveys` (
  `unique_id` int NOT NULL AUTO_INCREMENT,
  `owner_id` int NOT NULL,
  `item_id` int NOT NULL,
  `item_count` decimal(20,0) NOT NULL DEFAULT '1',
  `html_text` text NOT NULL,
  `html_radio` varchar(100) NOT NULL DEFAULT 'accept',
  `used` boolean NOT NULL DEFAULT '0',
  `used_time` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`unique_id`),
  KEY `owner_id` (`owner_id`),
  CONSTRAINT `surveys_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `towns`
-- ----------------------------
DROP TABLE IF EXISTS `towns`;
CREATE TABLE `towns` (
  `id` int NOT NULL,
  `level` int NOT NULL DEFAULT '1',
  `points` int NOT NULL DEFAULT '0',
  `race` enum('ELYOS','ASMODIANS') NOT NULL,
  `level_up_date` timestamp NOT NULL DEFAULT '1970-01-01 07:00:01',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
