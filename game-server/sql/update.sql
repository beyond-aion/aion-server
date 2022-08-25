/*
 * DB changes since d1b22999 (25.08.2022)
 */

-- replace incorrect items
UPDATE inventory SET item_id=188053183 AND item_skin=188053183 WHERE item_id=188053179;