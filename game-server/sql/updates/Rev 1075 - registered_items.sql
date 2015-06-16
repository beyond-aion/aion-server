ALTER TABLE `player_registered_items` DROP FOREIGN KEY `house_regitems_ibfk_1`;
ALTER TABLE `player_registered_items` ADD CONSTRAINT  
FOREIGN KEY `house_regitems_ibfk_1` (`address`) REFERENCES `houses` (`address`) 
ON DELETE NO ACTION ON UPDATE CASCADE;