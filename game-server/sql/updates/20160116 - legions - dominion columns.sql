ALTER TABLE `legions`
ADD COLUMN `occupied_legion_dominion` int(11) NOT NULL DEFAULT 0 AFTER `siege_glory_points`,
ADD COLUMN `last_legion_dominion` int(11) NOT NULL DEFAULT 0 AFTER `occupied_legion_dominion`,
ADD COLUMN `current_legion_dominion` int(11) NOT NULL DEFAULT 0 AFTER `last_legion_dominion`;