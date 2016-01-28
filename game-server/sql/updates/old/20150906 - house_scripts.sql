ALTER TABLE `house_scripts`
CHANGE COLUMN `index` `script_id`  tinyint(4) NOT NULL AFTER `house_id`,
MODIFY COLUMN `script`  mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `script_id`,
ADD COLUMN `date_added`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `script`;
