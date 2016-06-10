CREATE TABLE IF NOT EXISTS `headhunting` (
  `hunter_id` int(11) NOT NULL,
  `accumulated_kills`int(11) NOT NULL,
  `last_update` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`hunter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;