ALTER TABLE `player_quests`
ADD COLUMN `flags` int NOT NULL DEFAULT 0 after `quest_vars`;