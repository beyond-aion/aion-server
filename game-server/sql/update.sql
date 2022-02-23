/*
 * DB changes since 4307ddf3 (18.12.2021)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (188052318, 188100091, 188100092, 188100093, 188100094);