ALTER TABLE players ADD COLUMN item_expands tinyint(1) NOT NULL default 0 AFTER npc_expands;

UPDATE players SET item_expands = (quest_expands - 1), quest_expands = 1 WHERE quest_expands > 0 
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (1947,2937))
AND id NOT IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (1797,1800,2833)); # 1 quest completed

UPDATE players SET item_expands = (quest_expands - 1), quest_expands = 1 WHERE quest_expands > 0 
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (1797,1800,2833)) # 1 quest completed
AND id NOT IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (1947,2937));

UPDATE players SET item_expands = (quest_expands - 2), quest_expands = 2 WHERE quest_expands > 1
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (1947,2937)) # both quests completed
AND id IN (SELECT player_id FROM player_quests WHERE status = 'COMPLETE' AND quest_id IN (1797,1800,2833));

