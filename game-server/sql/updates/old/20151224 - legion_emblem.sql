ALTER TABLE `legion_emblems`
MODIFY COLUMN `emblem_id` tinyint(3) NOT NULL DEFAULT 0 AFTER `legion_id`,
MODIFY COLUMN `color_r` tinyint(3) NOT NULL DEFAULT 0 AFTER `emblem_id`,
MODIFY COLUMN `color_g` tinyint(3) NOT NULL DEFAULT 0 AFTER `color_r`,
MODIFY COLUMN `color_b` tinyint(3) NOT NULL DEFAULT 0 AFTER `color_g`,
ADD COLUMN `color_a` tinyint(3) NOT NULL DEFAULT 0 AFTER `emblem_id`;