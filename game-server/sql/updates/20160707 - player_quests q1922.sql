UPDATE player_quests t, (SELECT player_id FROM player_quests WHERE quest_id = 1921 AND `status` = "COMPLETE") ids
	SET t.`status` = "START"
	WHERE t.quest_id = 1922  AND t.`status` = "LOCKED" AND t.player_id = ids.player_id;
INSERT INTO player_quests (player_id, quest_id, `status`)
	SELECT t2.player_id, 1922 AS quest_id, "START" AS `status`
		FROM player_quests t2
		WHERE t2.quest_id = 1921 AND t2.`status` = "COMPLETE"
	ON DUPLICATE KEY UPDATE player_id = player_quests.player_id, quest_id = player_quests.quest_id, `status` = player_quests.`status`;