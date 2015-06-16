CREATE TABLE IF NOT EXISTS guild_quests (
  player_id int(11) NOT NULL,
  guild_id int(2) NOT NULL default '0',
  recently_taken_quest int(6) NOT NULL default '0',
  completion_timestamp timestamp NULL default NULL,
  currently_started_quest int(6) NOT NULL default '0',
  PRIMARY KEY  (player_id),
  FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;