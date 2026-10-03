/*
 * DB changes since a31c1df (29.09.2026)
 */

UPDATE `player_skills` SET `skill_level` = 1 WHERE `skill_id` < 30000;

ALTER TABLE `players`
	ADD COLUMN `pvp_exp` bigint NOT NULL DEFAULT '0' AFTER `reposte_energy`;
