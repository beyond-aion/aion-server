ALTER TABLE players ADD COLUMN npc_expands tinyint(1) NOT NULL default 0 AFTER cube_size;

UPDATE players SET npc_expands = (cube_size - 1) WHERE cube_size > 0 
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (2951,1947))
AND id NOT IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (2937,1564)); # 1 quest completed

UPDATE players SET npc_expands = (cube_size - 1) WHERE cube_size > 0 
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (2937,1564)) # 1 quest completed
AND id NOT IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (2951,1947));

UPDATE players SET npc_expands = (cube_size - 2) WHERE cube_size > 1 
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (2951,1947)) #both quests completed
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (2937,1564));

