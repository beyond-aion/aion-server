CREATE TABLE IF NOT EXISTS `spawns_loc` (
  `spawn_id` int(10) NOT NULL,
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL,
  `heading` tinyint(3) NOT NULL,
  KEY `fk_sp_id` (`spawn_id`),
  CONSTRAINT `fk_sp_id` FOREIGN KEY (`spawn_id`) REFERENCES `spawns` (`spawn_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

UPDATE spawns SET spawns.pool_size = 1;

INSERT INTO spawns_loc (spawn_id,x,y,z,heading) SELECT spawn_id,x,y,z,heading FROM spawns;

ALTER TABLE `spawns`
DROP COLUMN `x`,
DROP COLUMN `y`,
DROP COLUMN `z`,
DROP COLUMN `heading`;

