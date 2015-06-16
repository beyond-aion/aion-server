ALTER TABLE `account_data` ADD COLUMN `membership` tinyint(3) NOT NULL default '0' AFTER `access_level`;
