/*
 * DB changes since eed14e8 (22.04.2019)
 */

-- custom instance
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

-- cleanup
DROP TABLE IF EXISTS `advent`;

-- remove cooldowns of deleted players and add db constraint
DELETE FROM `house_object_cooldowns` WHERE `player_id` IN (SELECT * FROM (SELECT `player_id` FROM `house_object_cooldowns` LEFT JOIN `players` p ON `player_id` = p.id WHERE p.id IS NULL) t);
ALTER TABLE `house_object_cooldowns` ADD FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
-- remove cooldowns of deleted house objects and add db constraint
DELETE FROM `house_object_cooldowns` WHERE `object_id` IN (SELECT * FROM (SELECT `object_id` FROM `house_object_cooldowns` LEFT JOIN `player_registered_items` p ON `object_id` = `item_unique_id` WHERE `item_unique_id` IS NULL) t);
ALTER TABLE `player_registered_items` ADD INDEX (`item_unique_id`);
ALTER TABLE `house_object_cooldowns` ADD FOREIGN KEY (`object_id`) REFERENCES `player_registered_items` (`item_unique_id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- remove houses and studios of deleted players
DELETE FROM `houses` WHERE `player_id` IN (SELECT * FROM (SELECT `player_id` FROM `houses` h LEFT JOIN `players` p ON `player_id` = p.id WHERE `player_id` > 0 AND p.id IS NULL) t);
-- remove house of banned player with wrong state INACTIVE
DELETE FROM `houses` WHERE `player_id` = 125113;

-- change sign_notice to string
ALTER TABLE `houses` ADD COLUMN `tmp`  varchar(100) NULL AFTER `sign_notice`;
UPDATE `houses` SET `tmp` = CONVERT(`sign_notice` USING utf16le);
ALTER TABLE `houses`
    DROP COLUMN `sign_notice`,
    CHANGE COLUMN `tmp` `sign_notice` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `sell_started`;

-- remove obsolete field
ALTER TABLE `houses` DROP COLUMN `fee_paid`;

-- remove obsolete server vars
DELETE FROM `server_variables` WHERE `key` IN ("auctionTime", "houseMaintainTime", "auctionProlonged");

-- fix field properties
ALTER TABLE `house_bids` MODIFY COLUMN `bid_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `bid`;
ALTER TABLE `houses` MODIFY COLUMN `acquire_time` timestamp NULL DEFAULT NULL AFTER `address`;
UPDATE `houses` SET `acquire_time` = NULL WHERE `player_id` = 0;

-- remove obsolete house columns
ALTER TABLE `houses`
    DROP COLUMN `status`,
    DROP COLUMN `sell_started`;