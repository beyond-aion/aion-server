/*
 * DB changes since bf5cd5b0 (12.05.2023)
 */

-- drop advent calendar table
drop table advent;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (185000221, 185000222, 188600311);