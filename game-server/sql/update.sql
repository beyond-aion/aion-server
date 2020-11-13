/*
 * DB changes since 02a4e09 (13.11.2020)
 */

ALTER TABLE `abyss_rank`
    CHANGE COLUMN `rank` `rank` TINYINT NOT NULL DEFAULT 1 AFTER `ap`,
    CHANGE COLUMN `max_rank` `max_rank` TINYINT NOT NULL DEFAULT 1 AFTER `rank`,
    CHANGE COLUMN `rank_pos` `rank_pos` SMALLINT NOT NULL DEFAULT 0 AFTER `max_rank`,
    CHANGE COLUMN `old_rank_pos` `old_rank_pos` SMALLINT NOT NULL DEFAULT 0 AFTER `rank_pos`,
    DROP COLUMN `top_ranking`,
    ADD INDEX `rank` (`rank`),
    ADD INDEX `rank_pos` (`rank_pos`),
    ADD INDEX `gp` (`gp`);

ALTER TABLE `legions`
    CHANGE COLUMN `rank_pos` `rank_pos` SMALLINT NOT NULL DEFAULT 0 AFTER `disband_time`,
    CHANGE COLUMN `old_rank_pos` `old_rank_pos` SMALLINT NOT NULL DEFAULT 0 AFTER `rank_pos`,
    ADD INDEX `rank_pos` (`rank_pos`);