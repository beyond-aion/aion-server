ALTER TABLE `players`
MODIFY COLUMN `last_online`  timestamp NULL DEFAULT NULL AFTER `deletion_date`;