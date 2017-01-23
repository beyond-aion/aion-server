DELETE FROM `player_quests` WHERE status = "NONE" AND complete_count = 0;
UPDATE `player_quests` SET status = "COMPLETE" WHERE status = "NONE" AND complete_count > 0;
ALTER TABLE `player_quests`
MODIFY COLUMN `status`  enum('LOCKED','START','REWARD','COMPLETE') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `quest_id`;