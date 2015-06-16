ALTER TABLE `inventory`
ADD COLUMN `color_expires` int(11) NOT NULL DEFAULT '0' after `item_color`;