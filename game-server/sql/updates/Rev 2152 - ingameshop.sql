ALTER TABLE `ingameshop`
ADD COLUMN `sub_category` int(11) NOT NULL DEFAULT '0' after `category`,
ADD COLUMN `item_type` tinyint(1) NOT NULL DEFAULT '0' after `sales_ranking`,
ADD COLUMN `gift` tinyint(1) NOT NULL DEFAULT '0' after `item_type`,

MODIFY COLUMN `item_count` bigint(13) NOT NULL DEFAULT '0',
MODIFY COLUMN `item_price` bigint(13) NOT NULL DEFAULT '0',
MODIFY COLUMN `category` tinyint(1) NOT NULL DEFAULT '0';
