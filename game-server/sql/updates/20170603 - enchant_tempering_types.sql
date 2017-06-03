ALTER TABLE `inventory`
MODIFY COLUMN `enchant` tinyint(3) UNSIGNED,
MODIFY COLUMN `tempering` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 AFTER `rnd_count`;