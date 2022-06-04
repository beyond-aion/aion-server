/*
 * DB changes since 737e5e7d (31.05.2022)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (185000221);