/*
 * DB changes since 620fc69 (18.11.2026)
 */

ALTER TABLE `player_pets`
	ADD COLUMN `color` int NOT NULL DEFAULT '0' AFTER `decoration`,
	ADD COLUMN `face` int NOT NULL DEFAULT '0' AFTER `color`,
	ADD COLUMN `head` int NOT NULL DEFAULT '0' AFTER `face`;

ALTER TABLE `player_pets`
	DROP COLUMN `mood_started`,
	DROP COLUMN `counter`,
	DROP COLUMN `mood_cd_started`,
	DROP COLUMN `gift_cd_started`,
	DROP COLUMN `despawn_time`;

ALTER TABLE `house_bids`
	ADD COLUMN `step` bigint NOT NULL DEFAULT '100000' AFTER `bid`;
