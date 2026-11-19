/*
 * DB changes since 620fc69 (18.11.2026)
 */

ALTER TABLE `player_pets`
	ADD COLUMN `color` int NOT NULL DEFAULT '0' AFTER `decoration`,
	ADD COLUMN `face` int NOT NULL DEFAULT '0' AFTER `color`,
	ADD COLUMN `head` int NOT NULL DEFAULT '0' AFTER `face`;
