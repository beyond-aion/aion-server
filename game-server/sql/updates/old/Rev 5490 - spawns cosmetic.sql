ALTER TABLE `spawns`
CHANGE COLUMN `pool_id` `pool_size`  int(5) NOT NULL DEFAULT 1 AFTER `heading`;