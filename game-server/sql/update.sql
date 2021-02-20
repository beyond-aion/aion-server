/*
 * DB changes since f475323a (17.02.2021)
 */

-- drop advent calendar table
drop table advent;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (186000406, 186000407, 188053915, 188054028, 188054029);