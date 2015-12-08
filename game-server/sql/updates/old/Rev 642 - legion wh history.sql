ALTER TABLE `legion_history`
ADD COLUMN `tab_id` smallint(3) NOT NULL default '0' after `name`,
ADD COLUMN `description` varchar(30) NOT NULL DEFAULT '' after `tab_id`,

MODIFY COLUMN `history_type` enum('CREATE','JOIN','KICK','APPOINTED','EMBLEM_REGISTER','EMBLEM_MODIFIED','ITEM_DEPOSIT','ITEM_WITH_DRAW','KINAH_DOPOSIT','KINAH_WITH_DRAW') NOT NULL;