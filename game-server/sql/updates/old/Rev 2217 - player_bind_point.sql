-- -----------------------------
-- removes players bind_point --
-- -----------------------------
ALTER TABLE `players` DROP COLUMN `bind_point`;

-- --------------------
-- Create table `player_bind_point`
-- --------------------
CREATE TABLE IF NOT EXISTS `player_bind_point` (
  `player_id` int(11) NOT NULL,
  `map_id` int(11) NOT NULL,
  `x` FLOAT NOT NULL,
  `y` FLOAT NOT NULL,
  `z` FLOAT NOT NULL,
  `heading` int(3) NOT NULL,
  PRIMARY KEY (`player_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;
