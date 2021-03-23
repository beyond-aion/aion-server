/*
 * DB changes since 76cdcfce (23.03.2021)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (188051676, 188100057, 188100058, 188100059, 188100060);