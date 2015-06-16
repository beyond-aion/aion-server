ALTER TABLE `legion_history` CHANGE COLUMN `history_type` `history_type` ENUM('CREATE','JOIN','KICK','APPOINTED','EMBLEM_REGISTER','EMBLEM_MODIFIED','ITEM_DEPOSIT','ITEM_WITHDRAW','KINAH_DEPOSIT','KINAH_WITHDRAW','LEVEL_UP') NOT NULL AFTER `date`;
ALTER TABLE `legion_members` ADD COLUMN `challenge_score` INT(11) NOT NULL DEFAULT '0' AFTER `selfintro`;

CREATE TABLE `challenge_tasks` (
	`task_id` INT(11) NOT NULL,
	`quest_id` INT(10) NOT NULL,
	`owner_id` INT(11) NOT NULL,
	`owner_type` ENUM('LEGION','TOWN') NOT NULL,
	`complete_count` INT(3) UNSIGNED NOT NULL DEFAULT '0',
	`complete_time` TIMESTAMP NULL DEFAULT NULL,
	PRIMARY KEY (`task_id`, `quest_id`, `owner_id`, `owner_type`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `towns` (
	`id` INT(11) NOT NULL,
	`level` INT(11) NOT NULL DEFAULT '0',
	`points` INT(10) NOT NULL DEFAULT '0',
	`race` ENUM('ELYOS','ASMODIANS') NOT NULL,
	`level_up_date` TIMESTAMP NOT NULL DEFAULT '1970-01-01 07:00:01',
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
