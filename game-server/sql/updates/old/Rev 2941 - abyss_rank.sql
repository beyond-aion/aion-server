ALTER TABLE `abyss_rank`
 ADD COLUMN `rank_pos` INT(11) NOT NULL DEFAULT 0 AFTER `last_update`,
 ADD COLUMN `old_rank_pos` INT(11) NOT NULL DEFAULT 0 AFTER `rank_pos`,
 ADD COLUMN `rank_ap` INT(11) NOT NULL DEFAULT 0 AFTER `old_rank_pos`;

ALTER TABLE `legions`
 ADD COLUMN `rank_cp` INT(11) NOT NULL DEFAULT 0 AFTER `disband_time`,
 ADD COLUMN `rank_pos` INT(11) NOT NULL DEFAULT 0 AFTER `rank_cp`,
 ADD COLUMN `old_rank_pos` INT(11) NOT NULL DEFAULT 0 AFTER `rank_pos`;