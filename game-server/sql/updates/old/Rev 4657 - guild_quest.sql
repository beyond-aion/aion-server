-- ----------------------------
-- Table structure for `player_npc_factions`
-- ----------------------------
CREATE TABLE IF NOT EXISTS  `player_npc_factions` (
  `player_id` int(11) NOT NULL,
  `faction_id` int(2) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `time` int(11) NOT NULL,
  `state` enum('NOTING','START','COMPLETE') NOT NULL default 'NOTING',
  `quest_id` int(6) NOT NULL default '0',
  PRIMARY KEY  (`player_id`,`faction_id`),
  CONSTRAINT `player_npc_factions_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- updated for other tables
-- ----------------------------

DELETE FROM guild_quests;
ALTER TABLE `players` ADD COLUMN `mentor_flag_time` INT(11) NOT NULL DEFAULT 0 AFTER `note`;
