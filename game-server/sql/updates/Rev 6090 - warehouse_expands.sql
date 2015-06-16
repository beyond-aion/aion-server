ALTER table `players` CHANGE warehouse_size wh_npc_expands tinyint(1) NOT NULL default '0';
ALTER table `players` ADD COLUMN `wh_bonus_expands` tinyint(1) NOT NULL default '0' AFTER wh_npc_expands;

UPDATE players SET wh_npc_expands = (wh_npc_expands - 1), wh_bonus_expands = 1 WHERE wh_npc_expands > 0 
AND id IN (SELECT player_id FROM player_quests WHERE quest_id IN (1987,2985) AND status = 'COMPLETE'); # transfer quest expand