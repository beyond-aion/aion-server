ALTER TABLE `player_appearance`
 ADD COLUMN `face_contour` INT(11) UNSIGNED NOT NULL DEFAULT 0 AFTER `tattoo`,
 ADD COLUMN `expression` INT(11) UNSIGNED NOT NULL DEFAULT 0 AFTER `face_contour`,
 ADD COLUMN `jaw_line` INT(11) UNSIGNED NOT NULL DEFAULT 0 AFTER `expression`;