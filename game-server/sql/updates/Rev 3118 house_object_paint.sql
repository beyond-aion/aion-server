ALTER TABLE `player_registered_items`
ADD COLUMN `color` int(11) DEFAULT NULL after `expire_time`;
ALTER TABLE `player_registered_items`
ADD COLUMN `color_expires` int(11) NOT NULL DEFAULT '0' after `color`;