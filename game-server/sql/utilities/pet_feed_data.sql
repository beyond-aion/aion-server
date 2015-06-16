CREATE ALGORITHM=UNDEFINED 
DEFINER=`root`@`localhost` 
SQL SECURITY DEFINER VIEW `pet_feed_data` AS 
select 
`player_pets`.`player_id` AS `player_id`,
`player_pets`.`pet_id` AS `pet_id`,
`player_pets`.`name` AS `name`,
`player_pets`.`hungry_level` AS `hungry_level`,
(`player_pets`.`feed_progress` >> 24) AS `regular_count`,
(((`player_pets`.`feed_progress` & 0xfffc00) >> 10) * 4) AS `feed_points`,
((`player_pets`.`feed_progress` & 0x03f0) >> 4) AS `loved_count` 
from `player_pets` 
where (`player_pets`.`feed_progress` <> 0)