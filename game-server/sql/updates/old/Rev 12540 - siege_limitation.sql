-- ----------------------------
-- update siege_locations table
-- ----------------------------
ALTER TABLE `siege_locations` ADD COLUMN `occupy_count` TINYINT NOT NULL DEFAULT 0 AFTER `legion_id`;