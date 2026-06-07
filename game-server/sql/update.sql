/*
* DB changes since f2f77fe (15.05.2026)
 */

DELETE FROM inventory WHERE item_id IN (182007170, 188100252, 188100253, 188100254, 188100255, 188100256);

ALTER TABLE `bookmark`
	CHANGE COLUMN `char_id` `player_id` INT NOT NULL FIRST,
	CHANGE COLUMN `name` `name` VARCHAR(27) NOT NULL AFTER `player_id`,
	CHANGE COLUMN `world_id` `world_id` INT NOT NULL AFTER `name`,
	DROP COLUMN `id`,
	DROP PRIMARY KEY,
	ADD PRIMARY KEY (`player_id`, `name`),
	ADD CONSTRAINT `bookmark_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

DROP TABLE `ingameshop`;
DROP TABLE `ingameshop_log`;
