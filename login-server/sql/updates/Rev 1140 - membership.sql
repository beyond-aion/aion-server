ALTER TABLE `account_data` ADD COLUMN `old_membership` tinyint(3) NOT NULL  AFTER `membership` ;
ALTER TABLE `account_data` ADD COLUMN `expire` date DEFAULT NULL after `ip_force`;