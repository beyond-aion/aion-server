UPDATE `mail` set `sender_name` = SUBSTR(sender_name, 1, 26);
UPDATE `mail` set `mail_title` = SUBSTR(mail_title, 1, 16);

ALTER TABLE `mail`
MODIFY COLUMN `sender_name`  varchar(26) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `mail_recipient_id`,
MODIFY COLUMN `mail_title`  varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `sender_name`;