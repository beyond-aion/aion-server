CREATE TABLE `player_motions` (
  `player_id` int(11) NOT NULL,
  `motion_id` int(3) NOT NULL,
  `time` int(11) NOT NULL default '0',
  `active` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  USING BTREE (`player_id`,`motion_id`),
  CONSTRAINT `motions_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;