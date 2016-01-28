DELETE FROM `player_bind_point` WHERE `map_id` in(600020000, 600030000, 600040000, 600050000, 600060000, 600070000);

UPDATE `players` set 
  `world_id` = '110010000',
  `x` = '1313.25',
  `y` = '1512.011',
  `z` = '568.107',
  `heading` = '0'
WHERE `world_id` in(600020000, 600030000, 600040000, 600050000, 600060000, 600070000)
  AND `race` = 'ELYOS';

UPDATE `players` set 
  `world_id` = '120010000',
  `x` = '1685.7',
  `y` = '1400.5',
  `z` = '195.48618',
  `heading` = '60'
WHERE `world_id` in(600020000, 600030000, 600040000, 600050000, 600060000, 600070000)
  AND `race` = 'ASMODIANS';