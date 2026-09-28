/*
 * DB changes since c5a0f34 (12.09.2026)
 */

ALTER TABLE `player_effects`
	ADD COLUMN `magical_criticals` TINYINT NOT NULL DEFAULT '0' AFTER `force_type`;

ALTER TABLE `inventory`
	ADD COLUMN `rank_limit_expire_time` int NOT NULL DEFAULT '0' AFTER `rnd_plume_bonus`;
