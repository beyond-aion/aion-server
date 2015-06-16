-- ----------------------------
-- Table structure for `banned_mac`
-- ----------------------------
CREATE TABLE IF NOT EXISTS  `banned_mac` (
  `uniId` int(10) NOT NULL AUTO_INCREMENT,
  `address` varchar(20) NOT NULL,
  `time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,
  `details` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`uniId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;