UPDATE players SET cube_size = (cube_size - npc_expands);

ALTER TABLE `players`
CHANGE COLUMN `cube_size` `quest_expands`  tinyint(1) NOT NULL DEFAULT 0 AFTER `last_online`;