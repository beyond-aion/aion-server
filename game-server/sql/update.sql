/*
 * DB changes since a8d02131 (21.04.2024)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (186000406, 186000407, 188053915, 188054028, 188054029);