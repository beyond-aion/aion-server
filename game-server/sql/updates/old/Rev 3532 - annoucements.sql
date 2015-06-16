UPDATE `announcements` SET `type` = 'SYSTEM' WHERE `type` = 'ANNOUNCE';
UPDATE `announcements` SET `type` = 'WHITE' WHERE `type` = 'NORMAL';