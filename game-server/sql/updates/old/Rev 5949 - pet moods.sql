ALTER TABLE player_pets ADD COLUMN `mood_started` bigint(20) NOT NULL DEFAULT '0';
ALTER TABLE player_pets ADD COLUMN `counter` int(11) NOT NULL DEFAULT '0';
ALTER TABLE player_pets ADD COLUMN `mood_cd_started` bigint(20) NOT NULL DEFAULT '0';
ALTER TABLE player_pets ADD COLUMN `gift_cd_started` bigint(20) NOT NULL DEFAULT '0';
ALTER TABLE player_pets ADD COLUMN `despawn_time` timestamp NULL DEFAULT NULL;