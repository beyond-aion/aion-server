/*
 * DB changes since d1b22999 (02.07.2022)
 */

-- drop advent calendar table
drop table advent;

-- add 2 new columns for custom instance
ALTER TABLE `custom_instance`
ADD `max_rank` int(11) NOT NULL,
ADD `dps` int(11) NOT NULL;

UPDATE `custom_instance` SET `max_rank` = `rank`;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (186000111, 188051090, 188051091, 188051092, 188051093, 188051676, 188052625, 188052626, 188052627, 188100057, 188100058, 188100059, 188100060, 188100124, 188100125);