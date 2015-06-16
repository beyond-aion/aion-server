ALTER TABLE `inventory` 
DROP INDEX `item_location` 
, DROP INDEX `is_equiped` 
, DROP INDEX `item_owner` 
, ADD INDEX `item_location` USING HASH (`item_location`) 
, ADD INDEX `index3` (`item_owner`, `item_location`, `is_equiped`);
