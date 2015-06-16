-- -----------------------
-- ALTER
-- -----------------------
alter table `tasks` 

change `task_type` `task_type` enum('SHUTDOWN','RESTART','CLEAN_ACCOUNTS') character set utf8 collate utf8_general_ci NOT NULL, 
change `trigger_type` `trigger_type` enum('FIXED_IN_TIME','AFTER_RESTART') character set utf8 collate utf8_general_ci NOT NULL;

-- -----------------------
-- FOREIGN KEYS
-- -----------------------
delete FROM `account_rewards` WHERE `accountId` NOT IN (SELECT `id` FROM `account_data`);
alter table `account_rewards` add constraint `FK_account_rewards` FOREIGN KEY (`accountId`) REFERENCES `account_data` (`id`) ON DELETE CASCADE;

delete FROM `account_time` WHERE `account_id` NOT IN (SELECT `id` FROM `account_data`);
alter table `account_time` add constraint `FK_account_time` FOREIGN KEY (`account_id`) REFERENCES `account_data` (`id`) ON DELETE CASCADE;