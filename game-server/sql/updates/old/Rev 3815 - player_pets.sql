ALTER TABLE player_pets ADD COLUMN birthday timestamp NOT NULL default CURRENT_TIMESTAMP;
UPDATE player_pets SET birthday = CURRENT_TIMESTAMP;