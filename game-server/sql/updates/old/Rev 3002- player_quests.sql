ALTER TABLE player_quests
DROP completion_date;
ALTER TABLE player_quests
ADD  next_repeat_time timestamp NULL default NULL;