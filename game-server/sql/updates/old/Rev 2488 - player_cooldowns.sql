-- ---------------------------------- --
-- removes player_effects reuse_delay --
-- ---------------------------------- --
ALTER TABLE `player_effects` DROP COLUMN `reuse_delay`;

-- ------------------------------------------ --
-- deletes stored cooldowns in player_effects --
-- ------------------------------------------ --
DELETE FROM `player_effects` WHERE `current_time` = 0;

-- ------------------------------- --
-- Create table `player_cooldowns` --
-- ------------------------------- --
CREATE TABLE `player_cooldowns` (
  `player_id` int(11) NOT NULL,
  `cooldown_id` int(6) NOT NULL,
  `reuse_delay` bigint(13) NOT NULL,
  PRIMARY KEY (`player_id`,`cooldown_id`),
  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;