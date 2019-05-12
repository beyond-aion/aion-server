-- remove houses and studios of deleted players
DELETE FROM `houses` WHERE `player_id` IN (SELECT * FROM (SELECT `player_id` FROM `houses` h LEFT JOIN `players` p ON `player_id` = p.id WHERE `player_id` > 0 AND p.id IS NULL) t);
-- remove house of banned player with wrong state INACTIVE
DELETE FROM `houses` WHERE `player_id` = 125113;