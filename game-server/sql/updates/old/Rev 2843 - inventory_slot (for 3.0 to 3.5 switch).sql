ALTER TABLE `inventory` MODIFY COLUMN `slot` BIGINT(20) NOT NULL DEFAULT '0';

UPDATE `inventory` SET `slot` = (1 << 51) WHERE (`slot` & 0xFFFFFFFF) = (1 << 30);
UPDATE `inventory` SET `slot` = (1 << 52) WHERE (`slot` & 0xFFFFFFFF) = (1 << 31);

UPDATE `inventory` SET `slot` = (1 << 30) WHERE (`slot` & 0xFFFFFFFF) = (1 << 19);
UPDATE `inventory` SET `slot` = (1 << 31) WHERE (`slot` & 0xFFFFFFFF) = (1 << 20);
UPDATE `inventory` SET `slot` = (1 << 32) WHERE (`slot` & 0xFFFFFFFF) = (1 << 21);
UPDATE `inventory` SET `slot` = (1 << 33) WHERE (`slot` & 0xFFFFFFFF) = (1 << 22);
UPDATE `inventory` SET `slot` = (1 << 34) WHERE (`slot` & 0xFFFFFFFF) = (1 << 23);
UPDATE `inventory` SET `slot` = (1 << 35) WHERE (`slot` & 0xFFFFFFFF) = (1 << 24);

UPDATE `inventory` SET `slot` = (1 << 47) WHERE (`slot` & 0xFFFFFFFF) = (1 << 26);
UPDATE `inventory` SET `slot` = (1 << 48) WHERE (`slot` & 0xFFFFFFFF) = (1 << 27);
UPDATE `inventory` SET `slot` = (1 << 49) WHERE (`slot` & 0xFFFFFFFF) = (1 << 28);
UPDATE `inventory` SET `slot` = (1 << 50) WHERE (`slot` & 0xFFFFFFFF) = (1 << 29);
