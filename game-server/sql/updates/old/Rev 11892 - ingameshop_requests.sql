CREATE TABLE `ingameshop_requests` (
  `request_id` int(11) NOT NULL AUTO_INCREMENT,
  `item_id` int(11) NOT NULL,
  `item_count` bigint(21) NOT NULL,
  `buyer_character_name` varchar(20) NOT NULL,
  `receiver_character_name` varchar(20) NOT NULL,
  `delivered` tinyint(1) NOT NULL DEFAULT '0',
  `delivered_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;