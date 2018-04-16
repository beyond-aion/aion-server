ALTER TABLE `inventory`
CHANGE COLUMN `is_equiped` `is_equipped`  tinyint(1) NOT NULL DEFAULT 0 AFTER `item_owner`,
CHANGE COLUMN `rnd_count` `tune_count`  smallint(6) NOT NULL DEFAULT 0 AFTER `charge`,
MODIFY COLUMN `rnd_bonus`  smallint(6) NOT NULL DEFAULT 0 AFTER `tune_count`,
MODIFY COLUMN `tempering`  tinyint(3) UNSIGNED NOT NULL DEFAULT 0 ,
ADD COLUMN `fusion_rnd_bonus`  smallint(6) NOT NULL DEFAULT 0 AFTER `rnd_bonus`;