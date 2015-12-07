ALTER TABLE `portal_cooldowns`
ADD COLUMN `entry_count` int(3) NOT NULL DEFAULT 0 after `reuse_time`;