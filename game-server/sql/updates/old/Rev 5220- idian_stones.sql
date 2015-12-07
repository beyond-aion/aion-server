ALTER TABLE `item_stones`
ADD COLUMN `polishNumber` int NOT NULL DEFAULT 0 after `category`,
ADD COLUMN `polishCharge` int NOT NULL DEFAULT 0 after `polishNumber`;