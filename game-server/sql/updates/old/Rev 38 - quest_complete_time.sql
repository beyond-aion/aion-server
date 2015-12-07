ALTER TABLE `player_quests`
ADD COLUMN `complete_time` TIMESTAMP NULL DEFAULT NULL after `reward`;