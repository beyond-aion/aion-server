ALTER TABLE `players`
ADD COLUMN `old_level`  tinyint NOT NULL DEFAULT 0 AFTER `recoverexp`;