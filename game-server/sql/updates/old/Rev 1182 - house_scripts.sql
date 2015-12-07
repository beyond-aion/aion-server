CREATE TABLE `house_scripts` (
  `house_id` int(11) NOT NULL,
  `index` tinyint(4) NOT NULL,
  `script` mediumtext,
  PRIMARY KEY (`house_id`,`index`),
  CONSTRAINT `houses_id_ibfk_1` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=16;