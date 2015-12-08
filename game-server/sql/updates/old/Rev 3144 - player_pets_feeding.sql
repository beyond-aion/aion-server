ALTER TABLE `player_pets` DROP COLUMN `feed_points`;
UPDATE `player_pets` SET `feed_progress` = 0;