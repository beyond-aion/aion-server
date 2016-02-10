DELETE FROM `player_bind_point` WHERE `map_id` in(600020000, 600030000, 600040000, 600050000, 600060000, 600070000);

UPDATE `players` set 
  `world_id` = '210080000',
  `x` = '263.48',
  `y` = '191.30',
  `z` = '498.766',
  `heading` = '30'
WHERE `world_id` in(600020000, 600030000, 600040000, 600050000, 600060000, 600070000)
  AND `race` = 'ELYOS';

UPDATE `players` set 
  `world_id` = '220090000',
  `x` = '253.76',
  `y` = '159.6',
  `z` = '503.62',
  `heading` = '30'
WHERE `world_id` in(600020000, 600030000, 600040000, 600050000, 600060000, 600070000)
  AND `race` = 'ASMODIANS';