/*
 * DB changes since eed14e8 (22.04.2019)
 */

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
DELETE FROM `server_variables` WHERE `key` IN ("auctionTime", "houseMaintainTime");

-- fix field properties
ALTER TABLE `house_bids` MODIFY COLUMN `bid_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `bid`;
ALTER TABLE `houses` MODIFY COLUMN `acquire_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `address`;