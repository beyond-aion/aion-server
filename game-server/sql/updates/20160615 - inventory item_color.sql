UPDATE `inventory` i SET i.item_color = ((i.item_color & 0xFF000000) >> 24 | (i.item_color & 0xFF0000) >> 8 | (i.item_color & 0xFF00) << 8) WHERE `item_color` != 0;
ALTER TABLE `inventory` MODIFY COLUMN `item_color`  mediumint(8) UNSIGNED NULL DEFAULT NULL AFTER `item_count`;
UPDATE `inventory` SET `item_color` = NULL WHERE `item_color` = 0;
