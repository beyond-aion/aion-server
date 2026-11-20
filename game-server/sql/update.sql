/*
 * DB changes since 53363eea9 (11.11.2026)
 */

ALTER TABLE `players`
	ADD COLUMN `ap_from_users` bigint NOT NULL DEFAULT '0' AFTER `pvp_exp`,
	ADD COLUMN `salvation_energy` bigint NOT NULL DEFAULT '0' AFTER `ap_from_users`,
	ADD COLUMN `play_time` int NOT NULL DEFAULT '0' AFTER `salvation_energy`,
	ADD COLUMN `level_up_play_time` int NOT NULL DEFAULT '0' AFTER `play_time`;
