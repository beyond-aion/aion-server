/*
 * DB changes since 6eb8a81 (07.06.2024)
 */

ALTER TABLE `old_names`
	ADD COLUMN `renamed_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() AFTER `new_name`,
	ADD KEY `renamed_date` (`renamed_date`);

DROP TABLE `player_vars`;