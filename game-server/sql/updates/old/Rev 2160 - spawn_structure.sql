-- ----------------------------
-- Table structure for `spawns`
-- ----------------------------
DROP TABLE IF EXISTS `spawns`;
CREATE TABLE `spawns` (
  `spawn_id` int(10) NOT NULL auto_increment,
  `npc_id` int(10) NOT NULL,
  `npc_name` varchar(50) NOT NULL default '',
  `map_id` int(10) NOT NULL,
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `heading` tinyint(3) NOT NULL default 0,
  `pool_id` int(5) NOT NULL default 0,
  `anchor` varchar(100),
  `handler` enum('RIFT','STATIC') default NULL,
  `spawn_time` enum('ALL','DAY','NIGHT') NOT NULL default 'ALL',
  `walker_id` int(10) NOT NULL default '0',
  `random_walk` int(10) NOT NULL default '0',
  `static_id` int(10) NOT NULL default '0',
  `fly` tinyint(1) NOT NULL default '0',
  `respawn_time` int(10) NOT NULL default '0',
  `last_despawn_time` timestamp NULL default NULL,
  `date_added` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `author` varchar(50) NOT NULL default 'system',
  PRIMARY KEY (`spawn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT = 1000000;


-- ----------------------------------
-- Table structure for `siege_spawns`
-- ----------------------------------
DROP TABLE IF EXISTS `siege_spawns`;
CREATE TABLE `siege_spawns` (
  `spawn_id` int(10) NOT NULL,
  `siege_id` int(10) NOT NULL,
  `race` enum('ELYOS','ASMODIANS','BALAUR') NOT NULL,
  `protector` int(10) default '0',
  `stype` enum('PEACE','GUARD','ARTIFACT','PROTECTOR','MINE','PORTAL','GENERATOR','SPRING','RACEPROTECTOR','UNDERPASS') default NULL,
   PRIMARY KEY (`spawn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ---------------------------------
-- Table structure for `spawn_pools`
-- ---------------------------------
DROP TABLE IF EXISTS `spawn_pools`;
CREATE TABLE `spawn_pools` (
  `pool_id` int(5) NOT NULL auto_increment,
  `pool` tinyint(3) NOT NULL,
   PRIMARY KEY (`pool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `spawn_groups`;