CREATE TABLE `ingameshop_log` (
	`transaction_id` INT(11) NOT NULL AUTO_INCREMENT,
	`transaction_type` ENUM('BUY','GIFT') NOT NULL,
	`transaction_date` TIMESTAMP NULL DEFAULT NULL,
	`payer_name` VARCHAR(50) NOT NULL,
	`payer_account_name` VARCHAR(50) NOT NULL,
	`receiver_name` VARCHAR(50) NOT NULL,
	`item_id` INT(11) NOT NULL,
	`item_count` BIGINT(13) NOT NULL DEFAULT '0',
	`item_price` BIGINT(13) NOT NULL DEFAULT '0',
	PRIMARY KEY (`transaction_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
