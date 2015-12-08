-- ----------------------------
-- update broker table
-- ----------------------------
ALTER TABLE `broker` ADD COLUMN `splitting_available` TINYINT NOT NULL DEFAULT 0 AFTER `is_settled`;

UPDATE `broker` SET `price`=`price` / `item_count` WHERE item_count > 1;