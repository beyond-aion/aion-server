ALTER TABLE `player_pets`
DROP COLUMN `hungry_level`,
ADD COLUMN `hungry_level` tinyint NOT NULL DEFAULT 0 after `name`,
ADD COLUMN `feed_progress` int NOT NULL DEFAULT 0 after `hungry_level`,
ADD COLUMN `feed_points` tinyint NOT NULL DEFAULT 0 after `feed_progress`;