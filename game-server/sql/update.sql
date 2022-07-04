/*
 * DB changes since d1b22999 (02.07.2022)
 */

-- drop advent calendar table
drop table advent;

-- add 2 new columns for custom instance
ALTER TABLE `custom_instance`
ADD `max_rank` int(11) NOT NULL,
ADD `dps` int(11) NOT NULL;