/*
 * DB changes since 4bcc2bbf (12.01.2023)
 */

-- drop advent calendar table
drop table advent;

-- remove old event items
DELETE FROM inventory WHERE item_id IN (188052318, 188100091, 188100092, 188100093, 188100094);