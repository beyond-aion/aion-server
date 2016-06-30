ALTER TABLE `web_reward`
DROP COLUMN `rewarded`,
CHANGE COLUMN `unique` `entry_id`  int(11) NOT NULL AUTO_INCREMENT FIRST ,
CHANGE COLUMN `item_owner` `player_id`  int(11) NOT NULL AFTER `entry_id`,
MODIFY COLUMN `item_id`  int(9) NOT NULL AFTER `player_id`,
MODIFY COLUMN `item_count`  bigint(20) NOT NULL DEFAULT 1 AFTER `item_id`,
MODIFY COLUMN `added`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `item_count`,
MODIFY COLUMN `received`  timestamp NULL DEFAULT NULL AFTER `added`;
RENAME TABLE web_reward TO player_web_rewards;