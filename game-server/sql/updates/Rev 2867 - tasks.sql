-- ------------------------
-- ALTER
-- ------------------------
alter table `tasks`
drop column `last_activation`,
drop column `delay`,

change `task` `task_type` enum('SHUTDOWN','RESTART') NOT NULL, 
change `type` `trigger_type` enum('FIXED_IN_TIME') character set utf8 collate utf8_general_ci NOT NULL, 
change `start_time` `trigger_param` text NOT NULL,
change `param` `exec_param` text character set utf8 collate utf8_general_ci NULL;