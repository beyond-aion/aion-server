-- ----------------------------
-- update players table
-- ----------------------------
ALTER TABLE `players` DROP COLUMN `rewarded_pass`;

-- ----------------------------
-- Table structure for player_passports
-- ----------------------------
DROP TABLE IF EXISTS `player_passports`;
CREATE TABLE `player_passports` (
  `player_id` int(11) NOT NULL,
  `passportid` int(11) NOT NULL,
  `rewarded` int(11) NOT NULL,
  `arrive_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`passportid`,`arrive_date`),
  CONSTRAINT `player_passports` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8