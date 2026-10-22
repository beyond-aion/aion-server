/*
 * DB changes since a31c1df (29.09.2026)
 */

UPDATE `player_skills` SET `skill_level` = 1 WHERE (`skill_id` < 30000 OR `skill_id` = 40009);

ALTER TABLE `players`
	ADD COLUMN `pvp_exp` bigint NOT NULL DEFAULT '0' AFTER `reposte_energy`;

ALTER TABLE `player_recipes`
	ADD COLUMN `production_limit` tinyint NOT NULL DEFAULT '0' AFTER `recipe_id`;

ALTER TABLE `player_appearance`
	RENAME COLUMN `jaw_height` TO `chin_height`,
	RENAME COLUMN `chin_jut` TO `cheek_bones`;

ALTER TABLE `house_scripts`
	ADD COLUMN `script_data` blob NULL AFTER `script_id`,
	ADD COLUMN `uncompressed_size` int NOT NULL DEFAULT '0' AFTER `script_data`;
UPDATE `house_scripts` SET
	`uncompressed_size` = LENGTH(CONVERT(`script` USING utf16le)),
	`script_data` = SUBSTRING(COMPRESS(CAST(CONVERT(`script` USING utf16le) AS BINARY)), 5);
ALTER TABLE `house_scripts`
	DROP COLUMN `script`,
	CHANGE COLUMN `script_data` `script` blob NOT NULL,
	ALTER COLUMN `uncompressed_size` DROP DEFAULT;
