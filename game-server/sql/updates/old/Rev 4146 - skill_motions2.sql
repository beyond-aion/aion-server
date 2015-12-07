ALTER TABLE `skill_motions` 
   ADD COLUMN `race` VARCHAR(255) NOT NULL AFTER `off_weapon_type`, 
   ADD COLUMN `gender` VARCHAR(255) NOT NULL AFTER `race`,
   DROP PRIMARY KEY, 
   ADD PRIMARY KEY(`motion_name`, `skill_id`, `attack_speed`, `weapon_type`, `off_weapon_type`, `gender`) 
