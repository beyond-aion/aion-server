/*
 * DB changes since db3cab66 (18.04.2021)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (185000221, 185000222);