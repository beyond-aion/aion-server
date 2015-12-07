-- Switch house style from the 3rd to the 2nd (no more exists)
UPDATE `houses` SET building_id = building_id - 1 WHERE (building_id & 0xFF) = 2;
-- Delete player items for houses in grace period (unlucky players)
ALTER TABLE `player_registered_items` DROP FOREIGN KEY `player_registered_items_ibfk_1`;
ALTER TABLE `player_registered_items` DROP PRIMARY KEY, ADD PRIMARY KEY (`player_id`,`item_unique_id`,`item_id`);
ALTER TABLE `player_registered_items` ADD FOREIGN KEY player_registered_items_ibfk_1(`address`) REFERENCES houses(`address`) ON DELETE CASCADE ON UPDATE CASCADE;
DELETE FROM `houses` WHERE `status` = 'INACTIVE';
-- Delete default decorations
DELETE FROM `player_registered_items` WHERE item_unique_id = 0;
-- Delete house appearances (in XML now)
ALTER TABLE `houses` DROP COLUMN `roof`;
ALTER TABLE `houses` DROP COLUMN `outwall`;
ALTER TABLE `houses` DROP COLUMN `inwall`;
ALTER TABLE `houses` DROP COLUMN `infloor`;
ALTER TABLE `houses` DROP COLUMN `frame`;
ALTER TABLE `houses` DROP COLUMN `door`;
ALTER TABLE `houses` DROP COLUMN `garden`;
ALTER TABLE `houses` DROP COLUMN `fence`;
ALTER TABLE `houses` DROP COLUMN `addon`;
-- Change primary key
ALTER TABLE `houses` DROP PRIMARY KEY, ADD PRIMARY KEY (`id`);
