ALTER TABLE `player_registered_items` DROP FOREIGN KEY `player_registered_items_ibfk_1`;
ALTER TABLE `houses` DROP KEY `address`;
ALTER TABLE `houses` ADD KEY `address` (`address`);
ALTER TABLE `player_registered_items` ADD CONSTRAINT `player_registered_items_ibfk_1` FOREIGN KEY (`address`) REFERENCES `houses` (`address`) ON DELETE NO ACTION ON UPDATE CASCADE;
