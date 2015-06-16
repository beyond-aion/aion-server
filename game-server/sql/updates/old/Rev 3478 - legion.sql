ALTER TABLE `legions` DROP COLUMN `legionar_permission2`,
 DROP COLUMN `centurion_permission1`,
 DROP COLUMN `centurion_permission2`,
 ADD COLUMN `deputy_permission` INT(11) NOT NULL DEFAULT 7692 AFTER `contribution_points`,
 ADD COLUMN `centurion_permission` INT(11) NOT NULL DEFAULT 7176 AFTER `deputy_permission`,
 ADD COLUMN `legionary_permission` INT(11) NOT NULL DEFAULT 6144 AFTER `centurion_permission`,
 ADD COLUMN `volunteer_permission` INT(11) NOT NULL DEFAULT 2048 AFTER `legionary_permission`;

ALTER TABLE `legion_members` MODIFY COLUMN `rank` ENUM('BRIGADE_GENERAL','CENTURION','LEGIONARY','DEPUTY','VOLUNTEER') NOT NULL DEFAULT 'VOLUNTEER';
