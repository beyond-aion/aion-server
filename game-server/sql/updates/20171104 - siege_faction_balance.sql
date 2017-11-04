ALTER TABLE `siege_locations`
ADD COLUMN `faction_balance` tinyint(1) NOT NULL DEFAULT 0 AFTER `occupy_count`;