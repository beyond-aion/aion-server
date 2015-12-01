/*
Navicat MySQL Data Transfer

Source Server         : lokal
Source Server Version : 50621
Source Host           : localhost:3306
Source Database       : al_server_gs

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2015-12-01 03:13:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `advent`
-- ----------------------------
DROP TABLE IF EXISTS `advent`;
CREATE TABLE `advent` (
  `account_id` int(11) NOT NULL,
  `last_day_received` tinyint(4) NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
