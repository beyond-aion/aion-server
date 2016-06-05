ALTER TABLE `account_data`
ADD COLUMN `creation_date`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `password`;