-- ----------------------------
-- set all skills with level 10,11,12  to 9(summon fire spirit IV)
-- ----------------------------
UPDATE player_skills set skill_level=9 where skill_level in(10,11,12);
