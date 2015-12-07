DROP TABLE IF EXISTS `siege_mercenaries`;

CREATE TABLE `siege_mercenaries` (
  `location_id` int(11) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `race` enum('ELYOS','ASMODIANS') NOT NULL,
  PRIMARY KEY (`location_id`,`zone_id`),
  CONSTRAINT `siege_mercenaries_ibfk_1` FOREIGN KEY (`location_id`) REFERENCES `siege_locations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8