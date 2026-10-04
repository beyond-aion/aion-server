/*
 * DB changes since a31c1df (29.09.2026)
 */

UPDATE `player_skills` SET `skill_level` = 1 WHERE `skill_id` < 30000;
