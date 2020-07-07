/*
 * DB changes since ed3b91a (04.07.2020)
 */

-- remove old event items
DELETE FROM inventory WHERE item_id = 186000111;