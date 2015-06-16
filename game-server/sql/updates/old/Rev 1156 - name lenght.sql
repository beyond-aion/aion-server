ALTER TABLE `legion_members`
MODIFY COLUMN `nickname`  varchar(10);

ALTER TABLE `legion_members`
MODIFY COLUMN `selfintro`  varchar(32);

ALTER TABLE `legion_announcement_list`
MODIFY COLUMN `announcement`  varchar(256);
