-- ---------------------------------------
-- insert bind point to players (ELYOS) --
-- ---------------------------------------
INSERT INTO `player_bind_point`(
	`player_id`,
	`map_id`,
	`x`,
	`y`,
	`z`,
	`heading`
)SELECT
	id,
	210010000,
	1212.9423,
	1044.8516,
	140.75568,
	32
FROM
	players p
WHERE
	race = 'Elyos'
AND NOT EXISTS(
	SELECT
		*
	FROM
		player_bind_point pbp
	WHERE
		p.id = pbp.player_id
);


-- -------------------------------------------
-- insert bind point to players (ASMODIANS) --
-- -------------------------------------------
INSERT INTO `player_bind_point`(
	`player_id`,
	`map_id`,
	`x`,
	`y`,
	`z`,
	`heading`
)SELECT
	id,
	220010000,
	571.0388,
	2787.3420,
	299.8750,
	32
FROM
	players p
WHERE
	race = 'ASMODIANS'
AND NOT EXISTS(
	SELECT
		*
	FROM
		player_bind_point pbp
	WHERE
		p.id = pbp.player_id
);
