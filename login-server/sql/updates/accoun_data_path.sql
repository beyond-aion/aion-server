ALTER TABLE `account_data` ADD COLUMN `email` varchar(50) NOT NULL AFTER `toll`;
ALTER TABLE `account_data` ADD COLUMN `question` varchar(50) NOT NULL AFTER `email`;
ALTER TABLE `account_data` ADD COLUMN `answer` varchar(50) NOT NULL AFTER `question`;
ALTER TABLE `account_data` ADD COLUMN `balance` FLOAT NOT NULL AFTER `answer`;