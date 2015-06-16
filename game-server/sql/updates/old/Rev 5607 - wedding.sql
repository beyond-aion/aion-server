DROP TABLE IF EXISTS `weddings`;
CREATE TABLE `weddings` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `player1` int(11) NOT NULL,
  `player2` int(11) NOT NULL,
  PRIMARY KEY USING BTREE (`id`) ,
  KEY `player1` (`player1`),
  KEY `player2` (`player2`),
  CONSTRAINT `weddings_ibfk_2` FOREIGN KEY (`player2`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `weddings_ibfk_1` FOREIGN KEY (`player1`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;