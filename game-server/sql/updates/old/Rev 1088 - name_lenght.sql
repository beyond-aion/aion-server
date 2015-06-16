ALTER TABLE `broker`
MODIFY COLUMN `seller`  varchar(50);

ALTER TABLE `legions`
MODIFY COLUMN `name`  varchar(32);

ALTER TABLE `legion_history`
MODIFY COLUMN `name`  varchar(32);

ALTER TABLE `mail`
MODIFY COLUMN `sender_name`  varchar(50);