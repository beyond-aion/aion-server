SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for chatlog
-- ----------------------------

CREATE TABLE `chatlog` (
  `id` int(11) NOT NULL auto_increment,
  `sender` varchar(255) default NULL,
  `message` text NOT NULL,
  `type` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
