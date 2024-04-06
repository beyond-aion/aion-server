/*
 * DB changes since a66b0738 (06.04.2024)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (185000185, 185000186, 188052640, 188052642, 188100126, 188100127);