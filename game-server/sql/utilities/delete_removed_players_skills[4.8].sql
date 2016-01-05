DELETE FROM `player_effects`;

DELETE FROM `player_skills` WHERE `skill_id` NOT IN (2670,2671,30001,30002,30003,40001,40002,40003,40004,40005,40006,40007,40008,40009,40010);
UPDATE `player_skills` SET `skill_id` = 295 WHERE `skill_id` = 2670;
UPDATE `player_skills` SET `skill_id` = 296 WHERE `skill_id` = 2671;