-- ----------------------------
-- Table structure for `server_variables`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `server_variables` (
  `key` varchar(30) NOT NULL,
  `value` varchar(30) NOT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure `for players`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `players` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `account_id` int(11) NOT NULL,
  `account_name` varchar(50) NOT NULL,
  `exp` bigint(20) NOT NULL default '0',
  `recoverexp` bigint(20) NOT NULL default '0',
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `heading` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  `world_owner` int(11) NOT NULL default '0',
  `gender` enum('MALE','FEMALE') NOT NULL,
  `race` enum('ASMODIANS','ELYOS') NOT NULL,
  `player_class` enum('WARRIOR','GLADIATOR','TEMPLAR','SCOUT','ASSASSIN','RANGER','MAGE','SORCERER','SPIRIT_MASTER','PRIEST','CLERIC','CHANTER','ENGINEER','GUNNER','ARTIST','BARD','RIDER','ALL') NOT NULL,
  `creation_date` timestamp NULL default NULL,
  `deletion_date` timestamp NULL default NULL,
  `last_online` timestamp NULL default NULL on update CURRENT_TIMESTAMP,
  `quest_expands` tinyint(1) NOT NULL default '0',
  `npc_expands` tinyint(1) NOT NULL default '0',
  `item_expands` tinyint(1) NOT NULL default '0',
  `advenced_stigma_slot_size` TINYINT(1) NOT NULL DEFAULT '0',
  `wh_npc_expands` tinyint(1) NOT NULL default '0',
  `wh_bonus_expands` tinyint(1) NOT NULL default '0',
  `mailbox_letters` tinyint(4) UNSIGNED NOT NULL default '0',
  `title_id` int(3) NOT NULL default '-1',
  `bonus_title_id` int(3) NOT NULL default '-1',
  `dp` int(3) NOT NULL DEFAULT '0',
  `soul_sickness` tinyint(1) UNSIGNED NOT NULL DEFAULT '0',
  `reposte_energy` bigint(20) NOT NULL default '0',
  `online` tinyint(1) NOT NULL default '0',
  `note` text,
  `mentor_flag_time` INT(11) NOT NULL DEFAULT '0',
  `last_transfer_time` decimal(20) NOT NULL default '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`),
  INDEX (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_appearance`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `player_appearance` (
  `player_id` int(11) NOT NULL,
  `face` int(11) NOT NULL,
  `hair` int(11) NOT NULL,
  `deco` int(11) NOT NULL,
  `tattoo` int(11) NOT NULL,
  `face_contour` INT(11) NOT NULL,
  `expression` INT(11) NOT NULL,
  `jaw_line` INT(11) NOT NULL,
  `skin_rgb` int(11) NOT NULL,
  `hair_rgb` int(11) NOT NULL,
  `lip_rgb` int(11) NOT NULL,
  `eye_rgb` int(11) NOT NULL,
  `face_shape` int(11) NOT NULL,
  `forehead` int(11) NOT NULL,
  `eye_height` int(11) NOT NULL,
  `eye_space` int(11) NOT NULL,
  `eye_width` int(11) NOT NULL,
  `eye_size` int(11) NOT NULL,
  `eye_shape` int(11) NOT NULL,
  `eye_angle` int(11) NOT NULL,
  `brow_height` int(11) NOT NULL,
  `brow_angle` int(11) NOT NULL,
  `brow_shape` int(11) NOT NULL,
  `nose` int(11) NOT NULL,
  `nose_bridge` int(11) NOT NULL,
  `nose_width` int(11) NOT NULL,
  `nose_tip` int(11) NOT NULL,
  `cheek` int(11) NOT NULL,
  `lip_height` int(11) NOT NULL,
  `mouth_size` int(11) NOT NULL,
  `lip_size` int(11) NOT NULL,
  `smile` int(11) NOT NULL,
  `lip_shape` int(11) NOT NULL,
  `jaw_height` int(11) NOT NULL,
  `chin_jut` int(11) NOT NULL,
  `ear_shape` int(11) NOT NULL,
  `head_size` int(11) NOT NULL,
  `neck` int(11) NOT NULL,
  `neck_length` int(11) NOT NULL,
  `shoulders` int(11) NOT NULL,
  `shoulder_size` int(11) NOT NULL,
  `torso` int(11) NOT NULL,
  `chest` int(11) NOT NULL,
  `waist` int(11) NOT NULL,
  `hips` int(11) NOT NULL,
  `arm_thickness` int(11) NOT NULL,
  `arm_length` int(11) NOT NULL,
  `hand_size` int(11) NOT NULL,
  `leg_thickness` int(11) NOT NULL,
  `leg_length` int(11) NOT NULL,
  `foot_size` int(11) NOT NULL,
  `facial_rate` int(11) NOT NULL,
  `voice` int(11) NOT NULL,
  `height` float NOT NULL,
  PRIMARY KEY (`player_id`),
  CONSTRAINT `player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_macrosses`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `player_macrosses` (
  `player_id` int(11) NOT NULL,
  `order` int(3) NOT NULL,
  `macro` text NOT NULL,
  UNIQUE KEY `main` (`player_id`,`order`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_titles`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_titles` (
  `player_id` int(11) NOT NULL,
  `title_id` int(11) NOT NULL,
  `remaining` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`title_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `friends`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `friends` (
  `player` int(11) NOT NULL,
  `friend` int(11) NOT NULL,
  `memo` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`player`,`friend`),
  FOREIGN KEY (`player`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`friend`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `blocks`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `blocks` (
  `player` int(11) NOT NULL,
  `blocked_player` int(11) NOT NULL,
  `reason` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`player`,`blocked_player`),
  FOREIGN KEY (`player`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`blocked_player`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_settings`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `player_settings` (
  `player_id` int(11) NOT NULL,
  `settings_type` tinyint(1) NOT NULL,
  `settings` BLOB NOT NULL,
  PRIMARY KEY (`player_id`, `settings_type`),
  CONSTRAINT `ps_pl_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_skills`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `player_skills` (
  `player_id` int(11) NOT NULL,
  `skill_id` int(11) NOT NULL,
  `skill_level` int(3) NOT NULL default '1',
  PRIMARY KEY (`player_id`,`skill_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `inventory`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `inventory` (
  `item_unique_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_count` bigint(20) NOT NULL DEFAULT '0',
  `item_color` int(11) NOT NULL DEFAULT '0',
  `color_expires` int(11) NOT NULL DEFAULT '0',
  `item_creator` varchar(50),
  `expire_time` int(11) NOT NULL DEFAULT '0',
  `activation_count` int(11) NOT NULL DEFAULT '0',
  `item_owner` int(11) NOT NULL,
  `is_equiped` TINYINT(1) NOT NULL DEFAULT '0',
  `is_soul_bound` TINYINT(1) NOT NULL DEFAULT '0', 
  `slot` bigint(20) NOT NULL DEFAULT '0',
  `item_location` TINYINT(1) DEFAULT '0',
  `enchant` TINYINT(1) DEFAULT '0',
  `enchant_bonus` TINYINT(1) NOT NULL DEFAULT '0',
  `item_skin`  int(11) NOT NULL DEFAULT 0,
  `fusioned_item` INT(11) NOT NULL DEFAULT '0',
  `optional_socket` INT(1) NOT NULL DEFAULT '0',
  `optional_fusion_socket` INT(1) NOT NULL DEFAULT '0',
  `charge` MEDIUMINT NOT NULL DEFAULT '0',
  `rnd_bonus` smallint(6) DEFAULT NULL,
  `rnd_count` smallint(6) NOT NULL DEFAULT '0',
  `tempering` smallint(6) NOT NULL DEFAULT '0',
  `pack_count` smallint(6) NOT NULL DEFAULT '0',
  `is_amplified` tinyint(1) NOT NULL DEFAULT '0',
  `buff_skill` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`item_unique_id`),
  INDEX `item_location` USING HASH (`item_location`),
  INDEX `index3` (`item_owner`, `item_location`, `is_equiped`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `item_stones`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `item_stones` (
  `item_unique_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `slot` int(2) NOT NULL,
  `category` int(2) NOT NULL default 0,
  `polishNumber` int(11) NOT NULL,
  `polishCharge` int(11) NOT NULL,
  PRIMARY KEY (`item_unique_id`, `slot`, `category`),
  FOREIGN KEY (`item_unique_id`) references inventory (`item_unique_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_quests`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `player_quests` (
  `player_id` int(11) NOT NULL,
  `quest_id` int(10) unsigned NOT NULL default '0',
  `status` varchar(10) NOT NULL default 'NONE',
  `quest_vars` int(10) unsigned NOT NULL default '0',
  `flags` int(10) unsigned NOT NULL default '0',
  `complete_count` int(3) unsigned NOT NULL default '0',
  `next_repeat_time` timestamp NULL default NULL,
  `reward`  smallint(3) NULL,
  `complete_time` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`player_id`,`quest_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_npc_factions`
-- ----------------------------
CREATE TABLE IF NOT EXISTS  `player_npc_factions` (
  `player_id` int(11) NOT NULL,
  `faction_id` int(2) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `time` int(11) NOT NULL,
  `state` enum('NOTING','START','COMPLETE') NOT NULL default 'NOTING',
  `quest_id` int(6) NOT NULL default '0',
  PRIMARY KEY  (`player_id`,`faction_id`),
  CONSTRAINT `player_npc_factions_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `abyss_rank`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `abyss_rank` (
  `player_id` int(11) NOT NULL,
  `daily_ap` int(11) NOT NULL,
  `weekly_ap` int(11) NOT NULL,
  `ap` int(11) NOT NULL,
  `rank` int(2) NOT NULL DEFAULT '1',
  `top_ranking` int(4) NOT NULL,
  `daily_kill` int(5) NOT NULL,
  `weekly_kill` int(5) NOT NULL,
  `all_kill` int(4) NOT NULL DEFAULT '0',
  `max_rank` int(2) NOT NULL DEFAULT '1',
  `last_kill` int(5) NOT NULL,
  `last_ap` int(11) NOT NULL,
  `last_update` decimal(20,0) NOT NULL,
  `rank_pos` int(11) NOT NULL DEFAULT '0',
  `old_rank_pos` int(11) NOT NULL DEFAULT '0',
  `rank_ap` int(11) NOT NULL DEFAULT '0',
  `daily_gp` int(11) NOT NULL DEFAULT '0',
  `weekly_gp` int(11) NOT NULL DEFAULT '0',
  `gp` int(11) NOT NULL DEFAULT '0',
  `last_gp` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  CONSTRAINT `abyss_rank_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `legions`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `legions` (
  `id` int(11) NOT NULL,
  `name` varchar(32) NOT NULL,
  `level` int(1) NOT NULL DEFAULT '1',
  `contribution_points` bigint(20) NOT NULL DEFAULT '0',
  `deputy_permission` INT(11) NOT NULL DEFAULT 7692,
  `centurion_permission` INT(11) NOT NULL DEFAULT 7176,
  `legionary_permission` INT(11) NOT NULL DEFAULT 6144,
  `volunteer_permission` INT(11) NOT NULL DEFAULT 2048,
  `disband_time` int(11) NOT NULL default '0',
  `rank_pos` INT(11) NOT NULL DEFAULT '0',
  `old_rank_pos` INT(11) NOT NULL DEFAULT '0',
  `siege_glory_points` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `legion_announcement_list` (
  `legion_id` int(11) NOT NULL,
  `announcement` varchar(256) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `legion_members` (
  `legion_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `nickname` varchar(10) NOT NULL default '',  
  `rank` enum( 'BRIGADE_GENERAL','CENTURION','LEGIONARY','DEPUTY','VOLUNTEER' ) NOT NULL DEFAULT 'VOLUNTEER',
  `selfintro` varchar(32) default '',
  `challenge_score` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  KEY `player_id`(`player_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `legion_emblems` (
  `legion_id` int(11) NOT NULL,
  `emblem_id` int(1) NOT NULL default '0',
  `color_r` int(3) NOT NULL default '0',  
  `color_g` int(3) NOT NULL default '0', 
  `color_b` int(3) NOT NULL default '0',
  `emblem_type` enum('DEFAULT', 'CUSTOM') NOT NULL DEFAULT 'DEFAULT',
  `emblem_data` longblob,
  PRIMARY KEY (`legion_id`),
  FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `legion_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `legion_id` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `history_type` enum('CREATE','JOIN','KICK','APPOINTED','EMBLEM_REGISTER','EMBLEM_MODIFIED','ITEM_DEPOSIT','ITEM_WITHDRAW','KINAH_DEPOSIT','KINAH_WITHDRAW','LEVEL_UP') NOT NULL,
  `name` varchar(50) NOT NULL,
  `tab_id` smallint(3) NOT NULL default '0',
  `description` varchar(30) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`legion_id`) REFERENCES `legions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_recipes`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_recipes` (
  `player_id` int(11) NOT NULL,
  `recipe_id` int(11) NOT NULL,
  PRIMARY KEY (`player_id`,`recipe_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_punisments`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_punishments` (
  `player_id` int(11) NOT NULL,
  `punishment_type` enum('PRISON','GATHER','CHARBAN') NOT NULL,
  `start_time` int(10) unsigned default '0',
  `duration` int(10) unsigned default '0',
  `reason` text,
  PRIMARY KEY  (`player_id`,`punishment_type`),
  CONSTRAINT `player_punishments_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `mail_table`
-- ----------------------------
CREATE TABLE IF NOT EXISTS  `mail` (
  `mail_unique_id` int(11) NOT NULL,
  `mail_recipient_id` int(11) NOT NULL,
  `sender_name` varchar(20) character set utf8 NOT NULL,
  `mail_title` varchar(20) character set utf8 NOT NULL,
  `mail_message` varchar(1000) character set utf8 NOT NULL,
  `unread` tinyint(4) NOT NULL default '1',
  `attached_item_id` int(11) NOT NULL,
  `attached_kinah_count` bigint(20) NOT NULL,
  `express` tinyint(4) NOT NULL default '0', 
  `recieved_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY (`mail_unique_id`),
  KEY `mail_recipient_id` (`mail_recipient_id`),
  CONSTRAINT `FK_mail` FOREIGN KEY (`mail_recipient_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_effects`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_effects` (
  `player_id` int(11) NOT NULL,
  `skill_id` int(11) NOT NULL,
  `skill_lvl` tinyint NOT NULL,
  `current_time` int(11) NOT NULL,
  `end_time` bigint(13) NOT NULL,
  PRIMARY KEY (`player_id`, `skill_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `item_cooldowns`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `item_cooldowns` (
  `player_id` int(11) NOT NULL,
  `delay_id` int(11) NOT NULL,
  `use_delay` INTEGER UNSIGNED NOT NULL,
  `reuse_time` BIGINT(13) NOT NULL,
  PRIMARY KEY (`player_id`, `delay_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `broker`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `broker` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_pointer` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL,
  `item_count` bigint(20) NOT NULL,
  `item_creator` varchar(50),
  `seller` varchar(50) NOT NULL,
  `price` bigint(20) NOT NULL DEFAULT '0',
  `broker_race` enum('ELYOS','ASMODIAN') NOT NULL,
  `expire_time` timestamp NOT NULL DEFAULT '2010-01-01 02:00:00',
  `settle_time` timestamp NOT NULL DEFAULT '2010-01-01 02:00:00',
  `seller_id` int(11) NOT NULL,
  `is_sold` tinyint(1) NOT NULL,
  `is_settled` tinyint(1) NOT NULL,
  `splitting_available` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`seller_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `bookmark`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bookmark` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(50) default NULL,
  `char_id` int(11) NOT NULL,
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `world_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `announcements`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `announcements` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `announce` text NOT NULL,
  `faction` enum('ALL','ASMODIANS','ELYOS') NOT NULL DEFAULT 'ALL',
  `type` enum('SHOUT','ORANGE','YELLOW','WHITE','SYSTEM') NOT NULL default 'SYSTEM',
  `delay` int(4) NOT NULL DEFAULT '1800',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_life_stats`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `player_life_stats` (
  `player_id` INT(11) NOT NULL ,
  `hp` INT(11) NOT NULL DEFAULT 1 ,
  `mp` INT(11) NOT NULL DEFAULT 1 ,
  `fp` INT(11) NOT NULL DEFAULT 1 ,
  PRIMARY KEY (`player_id`) ,
  CONSTRAINT `FK_player_life_stats` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `siege_locations`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `siege_locations` (
  `id` int(11) NOT NULL,
  `race` enum('ELYOS', 'ASMODIANS', 'BALAUR') NOT NULL,
  `legion_id` int (11) NOT NULL,
  `occupy_count` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `siege_mercenaries`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `siege_mercenaries` (
  `location_id` int(11) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `race` enum('ELYOS','ASMODIANS') NOT NULL,
  PRIMARY KEY (`location_id`,`zone_id`),
  CONSTRAINT `siege_mercenaries_ibfk_1` FOREIGN KEY (`location_id`) REFERENCES `siege_locations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `tasks`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tasks` (
  `id` int(5) NOT NULL auto_increment,
  `task_type` enum('SHUTDOWN','RESTART') NOT NULL,
  `trigger_type` enum('FIXED_IN_TIME') NOT NULL,
  `trigger_param` text NOT NULL,
  `exec_param` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_pets`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_pets` (
  `player_id` int(11) NOT NULL,
  `pet_id` int(11) NOT NULL,
  `decoration` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `hungry_level` tinyint NOT NULL DEFAULT '0',
  `feed_progress` int NOT NULL DEFAULT '0',
  `reuse_time` bigint(20) NOT NULL DEFAULT '0',
  `birthday` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `mood_started` bigint(20) NOT NULL DEFAULT '0',
  `counter` int(11) NOT NULL DEFAULT '0',
  `mood_cd_started` bigint(20) NOT NULL DEFAULT '0',
  `gift_cd_started` bigint(20) NOT NULL DEFAULT '0',
  `dopings` varchar(80) CHARACTER SET ascii NULL DEFAULT NULL,
  `despawn_time` timestamp NULL DEFAULT NULL,
  `expire_time` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`, `pet_id`),
  CONSTRAINT `FK_player_pets` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `portal_cooldowns`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `portal_cooldowns` (
  `player_id` int(11) NOT NULL,
  `world_id` int(11) NOT NULL,
  `reuse_time` BIGINT(13) NOT NULL,
  `entry_count` int(11) NOT NULL,
  PRIMARY KEY (`player_id`, `world_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_passkey`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_passkey` (
  `account_id` int(11) NOT NULL,
  `passkey` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`account_id`,`passkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `guides`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `guides` (
  `guide_id` int(11) NOT NULL auto_increment,
  `player_id` int(11) NOT NULL,
  `title` varchar(80) NOT NULL,
  PRIMARY KEY (`guide_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `ingameshop`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ingameshop` (
  `object_id` int(11) NOT NULL auto_increment,
  `item_id` int(11) NOT NULL,
  `item_count` bigint(13) NOT NULL DEFAULT '0',
  `item_price` bigint(13) NOT NULL DEFAULT '0',
  `category` tinyint(1) NOT NULL DEFAULT '0',
  `sub_category` tinyint(1) NOT NULL DEFAULT '0',
  `list` int(11) NOT NULL DEFAULT '0',
  `sales_ranking` int(11) NOT NULL DEFAULT '0',
  `item_type` tinyint(1) NOT NULL DEFAULT '0',
  `gift` tinyint(1) NOT NULL DEFAULT '0',
  `title_description` varchar(20) NOT NULL,
  `description` varchar(20) NOT NULL,
  PRIMARY KEY (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `ingameshop_log`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ingameshop_log` (
	`transaction_id` INT(11) NOT NULL AUTO_INCREMENT,
	`transaction_type` ENUM('BUY','GIFT') NOT NULL,
	`transaction_date` TIMESTAMP NULL DEFAULT NULL,
	`payer_name` VARCHAR(50) NOT NULL,
	`payer_account_name` VARCHAR(50) NOT NULL,
	`receiver_name` VARCHAR(50) NOT NULL,
	`item_id` INT(11) NOT NULL,
	`item_count` BIGINT(13) NOT NULL DEFAULT '0',
	`item_price` BIGINT(13) NOT NULL DEFAULT '0',
	PRIMARY KEY (`transaction_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

-- ----------------------------
-- Table structure for `ingameshop_requests`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ingameshop_requests` (
	`request_id` int(11) NOT NULL AUTO_INCREMENT,
	`item_id` int(11) NOT NULL,
	`item_count` bigint(21) NOT NULL,
	`buyer_character_name` varchar(20) NOT NULL,
	`receiver_character_name` varchar(20) NOT NULL,
	`delivered` tinyint(1) NOT NULL DEFAULT '0',
	`delivered_at` timestamp NULL DEFAULT NULL,
	PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `old_names`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `old_names` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `old_name` varchar(50) NOT NULL,
  `new_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_emotions`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_emotions` (
  `player_id` int(11) NOT NULL,
  `emotion` int(11) NOT NULL,
  `remaining` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`player_id`, `emotion`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_bind_point`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_bind_point` (
  `player_id` int(11) NOT NULL,
  `map_id` int(11) NOT NULL,
  `x` FLOAT NOT NULL,
  `y` FLOAT NOT NULL,
  `z` FLOAT NOT NULL,
  `heading` int(3) NOT NULL,
  PRIMARY KEY (`player_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_cooldowns`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_cooldowns` (
  `player_id` int(11) NOT NULL,
  `cooldown_id` int(6) NOT NULL,
  `reuse_delay` bigint(13) NOT NULL,
  PRIMARY KEY (`player_id`,`cooldown_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `craft_cooldowns`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `craft_cooldowns` (
  `player_id` int(11) NOT NULL,
  `delay_id` int(11) unsigned NOT NULL,
  `reuse_time` bigint(13) unsigned NOT NULL,
  PRIMARY KEY (`player_id`,`delay_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `surveys`
-- ----------------------------
DROP TABLE IF EXISTS `surveys`;
CREATE TABLE `surveys` (
  `unique_id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_count` decimal(20,0) NOT NULL DEFAULT '1',
  `html_text` text NOT NULL,
  `html_radio` varchar(100) NOT NULL DEFAULT 'accept',
  `used` tinyint(1) NOT NULL DEFAULT '0',
  `used_time` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`unique_id`),
  KEY `owner_id` (`owner_id`),
  CONSTRAINT `surveys_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_motions`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `player_motions` (
  `player_id` int(11) NOT NULL,
  `motion_id` int(3) NOT NULL,
  `time` int(11) NOT NULL default '0',
  `active` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  USING BTREE (`player_id`,`motion_id`),
  CONSTRAINT `motions_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `web_reward`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `web_reward` (
  `unique` int(11) NOT NULL AUTO_INCREMENT,
  `item_owner` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_count` decimal(20,0) NOT NULL DEFAULT '1',
  `rewarded` tinyint(1) NOT NULL DEFAULT '0',
  `added` varchar(70) DEFAULT '',
  `received` varchar(70) DEFAULT '',
  PRIMARY KEY (`unique`),
  FOREIGN KEY (`item_owner`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `player_vars`
-- ----------------------------
DROP TABLE IF EXISTS `player_vars`;
CREATE TABLE `player_vars` (
  `player_id` int(11) NOT NULL,
  `param` varchar(255) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `time` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`player_id`,`param`),
  CONSTRAINT `player_vars_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `houses`;
CREATE TABLE `houses` (
  `id` int(10) NOT NULL,
  `player_id` int(10) NOT NULL DEFAULT '0',
  `building_id` int(10) NOT NULL,
  `address` int(10) NOT NULL,
  `acquire_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `settings` int(11) NOT NULL DEFAULT '0',
  `status` enum('ACTIVE','SELL_WAIT','INACTIVE','NOSALE') NOT NULL DEFAULT 'ACTIVE',
  `fee_paid` tinyint(1) NOT NULL DEFAULT '1',
  `next_pay` timestamp NULL DEFAULT NULL,
  `sell_started` timestamp NULL DEFAULT NULL,
  `sign_notice` binary(130) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `player_registered_items`;
CREATE TABLE `player_registered_items` (
  `player_id` int(10) NOT NULL,
  `item_unique_id` int(10) NOT NULL,
  `item_id` int(10) NOT NULL,
  `expire_time` int(20) DEFAULT NULL,
  `color` int(11) DEFAULT NULL,
  `color_expires` int(11) NOT NULL DEFAULT '0',
  `owner_use_count` int(10) NOT NULL DEFAULT '0',
  `visitor_use_count` int(10) NOT NULL DEFAULT '0',
  `x` float NOT NULL DEFAULT '0',
  `y` float NOT NULL DEFAULT '0',
  `z` float NOT NULL DEFAULT '0',
  `h` smallint(3) DEFAULT NULL,
  `area` enum('NONE','INTERIOR','EXTERIOR','ALL','DECOR') NOT NULL DEFAULT 'NONE',
  `room` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`,`item_unique_id`,`item_id`),
  CONSTRAINT `player_regitems_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `house_bids`;
CREATE TABLE `house_bids` (
  `player_id` int(10) NOT NULL,
  `house_id` int(10) NOT NULL,
  `bid` bigint(20) NOT NULL,
  `bid_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`house_id`,`bid`),
  KEY `house_id_ibfk_1` (`house_id`),
  CONSTRAINT `house_id_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `house_scripts`
-- ----------------------------
DROP TABLE IF EXISTS `house_scripts`;
CREATE TABLE `house_scripts` (
  `house_id` int(11) NOT NULL,
  `script_id` tinyint(4) NOT NULL,
  `script` mediumtext NOT NULL,
  `date_added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`house_id`,`script_id`),
  CONSTRAINT `houses_id_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=16;

DROP TABLE IF EXISTS `house_object_cooldowns`;
CREATE TABLE `house_object_cooldowns` (
  `player_id` int(11) NOT NULL,
  `object_id` int(11) NOT NULL,
  `reuse_time` bigint(20) NOT NULL,
  PRIMARY KEY (`player_id`,`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `challenge_tasks`;
CREATE TABLE `challenge_tasks` (
	`task_id` INT(11) NOT NULL,
	`quest_id` INT(10) NOT NULL,
	`owner_id` INT(11) NOT NULL,
	`owner_type` ENUM('LEGION','TOWN') NOT NULL,
	`complete_count` INT(3) UNSIGNED NOT NULL DEFAULT '0',
	`complete_time` TIMESTAMP NULL DEFAULT NULL,
	PRIMARY KEY (`task_id`, `quest_id`, `owner_id`, `owner_type`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

DROP TABLE IF EXISTS `towns`;
CREATE TABLE `towns` (
	`id` INT(11) NOT NULL,
	`level` INT(11) NOT NULL DEFAULT '1',
	`points` INT(10) NOT NULL DEFAULT '0',
	`race` ENUM('ELYOS','ASMODIANS') NOT NULL,
	`level_up_date` TIMESTAMP NOT NULL DEFAULT '1970-01-01 07:00:01',
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

DROP TABLE IF EXISTS `skill_motions`;
CREATE TABLE `skill_motions` (
  `motion_name` varchar(255) NOT NULL DEFAULT '',
  `skill_id` int(11) NOT NULL,
  `attack_speed` int(11) NOT NULL,
  `weapon_type` varchar(255) NOT NULL,
  `off_weapon_type` varchar(255) NOT NULL,
  `race` varchar(255) NOT NULL,
  `gender` varchar(255) NOT NULL,
  `time` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`motion_name`,`skill_id`,`attack_speed`,`weapon_type`,`off_weapon_type`,`gender`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `event_rank`;
CREATE TABLE `event_rank` (
  `player_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `wins` int(11) NOT NULL,
  `loss` int(11) NOT NULL,
  PRIMARY KEY (`player_id`,`event_id`),
  CONSTRAINT `event_rank_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `commands_access`;
CREATE TABLE `commands_access` (
  `player_id` int(11) NOT NULL,
  `command` varchar(40) NOT NULL,
  PRIMARY KEY (`player_id`,`command`),
  CONSTRAINT `commands_access_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for account_passports
-- ----------------------------
DROP TABLE IF EXISTS `account_passports`;
CREATE TABLE `account_passports` (
  `account_id` int(11) NOT NULL,
  `passport_id` int(11) NOT NULL,
  `rewarded` int(11) NOT NULL,
  `arrive_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`passport_id`,`arrive_date`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8

-- ----------------------------
-- Table structure for `account_stamps`
-- ----------------------------
DROP TABLE IF EXISTS `account_stamps`;
CREATE TABLE `account_stamps` (
  `account_id` int(11) NOT NULL,
  `stamps` tinyint(2) NOT NULL DEFAULT '0',
  `last_stamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;