ALTER TABLE `player_punishments` 
ADD COLUMN `reason` TEXT NULL AFTER `duration`,

CHANGE `punishment_type` `punishment_type` ENUM('PRISON','GATHER','CHARBAN') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL, 
CHANGE `punishment_status` `start_time` INT(10) UNSIGNED DEFAULT '0' NULL , 
CHANGE `punishment_timer` `duration` INT(10) UNSIGNED DEFAULT '0' NULL;