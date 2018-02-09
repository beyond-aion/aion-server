ALTER TABLE `player_pets`
CHANGE COLUMN `pet_id` `template_id`  int(11) NOT NULL AFTER `player_id`,
ADD COLUMN `id`  int NULL FIRST ;