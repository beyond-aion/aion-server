/*
 * DB changes since a152e86 (09.06.2024)
 */

UPDATE account_login_history
SET hdd_serial = CONCAT("0x", HEX(CONVERT(hdd_serial USING UTF16LE)))
WHERE hdd_serial != "" AND (LENGTH(hdd_serial) <= 2 OR hdd_serial NOT RLIKE "^[0-9a-zA-Z _-]+$");

UPDATE account_data
SET last_hdd_serial = CONCAT("0x", HEX(CONVERT(last_hdd_serial USING UTF16LE)))
WHERE last_hdd_serial != "" AND (LENGTH(last_hdd_serial) <= 2 OR last_hdd_serial NOT RLIKE "^[0-9a-zA-Z _-]+$");