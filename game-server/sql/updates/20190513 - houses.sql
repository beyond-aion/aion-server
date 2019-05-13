ALTER TABLE `houses`
ADD COLUMN `tmp`  varchar(100) NULL AFTER `sign_notice`;
UPDATE `houses` SET `tmp` = CONVERT(`sign_notice` USING utf16le);
ALTER TABLE `houses`
DROP COLUMN `sign_notice`,
CHANGE COLUMN `tmp` `sign_notice` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `sell_started`;
