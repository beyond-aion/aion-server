/*
 * DB changes since 2947cbf0 (05.08.2023)
 */

-- drop advent calendar table
drop table advent;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (186000111, 188051090, 188051091, 188052625, 188052626, 188052627, 188100124, 188100125);