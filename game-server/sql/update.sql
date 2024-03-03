/*
 * DB changes since feb36dd6 (04.03.2024)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id IN (186000389);