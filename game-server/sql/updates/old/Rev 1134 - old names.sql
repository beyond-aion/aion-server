DROP TABLE IF EXISTS `old_names`;
CREATE TABLE `old_names` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `old_name` varchar(50) NOT NULL,
  `new_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;