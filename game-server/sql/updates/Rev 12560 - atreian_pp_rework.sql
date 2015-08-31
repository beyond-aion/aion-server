-- Rework Atreian Passports to be account-bound
-- @author Luzien

ALTER TABLE player_passports DROP FOREIGN KEY `player_passports`;
RENAME TABLE player_passports TO account_passports;
ALTER table account_passports CHANGE arrive_date arrive_date timestamp NOT NULL default CURRENT_TIMESTAMP;

--
-- This query tries to migrate old player-bound passports to new account-bound system.
-- You should check data afterwards for 0 account_ids.
--
-- Easy solution is to reset all passport data. In this case uncomment the first query and comment the second. But not collected rewards will be lost.
--
-- TRUNCATE TABLE account_passports;
UPDATE account_passports SET account_passports.player_id = (SELECT players.account_id from players WHERE players.id = account_passports.player_id);



ALTER TABLE account_passports CHANGE player_id account_id int(11) NOT NULL;

CREATE TABLE `account_stamps` (
  `account_id` int(11) NOT NULL,
  `stamps` tinyint(2) NOT NULL default '0',
  `last_stamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- migrate `stamps` and `last_stamp` from players to new table. Use highest stamp count of accounts characters
INSERT INTO account_stamps (account_stamps.account_id, account_stamps.stamps, account_stamps.last_stamp)
SELECT players.account_id, players.stamps, players.last_stamp FROM players
WHERE players.stamps > 0
ON DUPLICATE KEY UPDATE account_stamps.stamps = CASE WHEN account_stamps.stamps < VALUES(stamps) 
                                    THEN VALUES(stamps) ELSE account_stamps.stamps END,
                                    account_stamps.last_stamp = CASE WHEN account_stamps.last_stamp IS NULL 
                                    THEN VALUES(last_stamp) ELSE account_stamps.last_stamp END;

ALTER TABLE players DROP stamps, DROP last_stamp;