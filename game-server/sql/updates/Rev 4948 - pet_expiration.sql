ALTER table `player_pets` 
ADD COLUMN `expire_time` int(11) NOT NULL DEFAULT '0' AFTER `despawn_time`;