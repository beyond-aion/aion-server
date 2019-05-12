-- remove cooldowns of deleted players and add db constraint
DELETE FROM `house_object_cooldowns` WHERE `player_id` IN (SELECT * FROM (SELECT `player_id` FROM `house_object_cooldowns` LEFT JOIN `players` p ON `player_id` = p.id WHERE p.id IS NULL) t);
ALTER TABLE `house_object_cooldowns` ADD FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
-- remove cooldowns of deleted house objects and add db constraint
DELETE FROM `house_object_cooldowns` WHERE `object_id` IN (SELECT * FROM (SELECT `object_id` FROM `house_object_cooldowns` LEFT JOIN `player_registered_items` p ON `object_id` = `item_unique_id` WHERE `item_unique_id` IS NULL) t);
ALTER TABLE `player_registered_items` ADD INDEX (`item_unique_id`);
ALTER TABLE `house_object_cooldowns` ADD FOREIGN KEY (`object_id`) REFERENCES `player_registered_items` (`item_unique_id`) ON DELETE CASCADE ON UPDATE CASCADE;