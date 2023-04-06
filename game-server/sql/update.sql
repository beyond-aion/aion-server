/*
 * DB changes since 4bb39c06 (20.02.2023)
 */

-- drop advent calendar table
drop table advent;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (186000175, 188051676, 188100057, 188100058, 188100059, 188100060);