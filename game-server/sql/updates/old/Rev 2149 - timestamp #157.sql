-- ----------------------------
-- changes the players creation_date, last_online so the default is NULL.
-- ----------------------------
ALTER TABLE `players` CHANGE COLUMN `creation_date` `creation_date` timestamp NULL default NULL;
ALTER TABLE `players` CHANGE COLUMN `last_online` `last_online` timestamp NULL default NULL on update CURRENT_TIMESTAMP;

-- ----------------------------
-- Updates the players creation_date, deletion date, last_online so any corrupted records are set to NULL.
-- ----------------------------
UPDATE `players` SET `creation_date` = NULL WHERE `creation_date` = '00-00-00 00:00:00';
UPDATE `players` SET `deletion_date` = NULL WHERE `deletion_date` = '00-00-00 00:00:00';
UPDATE `players` SET `last_online` = NULL WHERE `last_online` = '00-00-00 00:00:00';