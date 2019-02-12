package com.aionemu.gameserver.model;

/**
 * @author lyahim
 */
public enum EmotionType {
	UNK(-1),
	SELECT_TARGET(0),
	JUMP(1),
	SIT(2),
	STAND(3),
	CHAIR_SIT(4),
	CHAIR_UP(5),
	START_FLYTELEPORT(6),
	LAND_FLYTELEPORT(7),
	WINDSTREAM(8),
	WINDSTREAM_END(9),
	WINDSTREAM_EXIT(10),
	WINDSTREAM_START_BOOST(11),
	WINDSTREAM_END_BOOST(12),
	FLY(13),
	LAND(14),
	RIDE(15),
	RIDE_END(16),
	ATTACK(17),
	DIE(18),
	RESURRECT(19),
	EMOTE(21),
	END_DUEL(22), // What? Duel? It's the end of a emote
	ATTACKMODE_IN_MOVE(24),
	NEUTRALMODE_IN_MOVE(25),
	WALK(26),
	RUN(27),
	OPEN_DOOR(31),
	CLOSE_DOOR(32),
	OPEN_PRIVATESHOP(33),
	CLOSE_PRIVATESHOP(34),
	START_EMOTE2(35), // It's not "emote". Triggered after Attack Mode of npcs
	POWERSHARD_ON(36),
	POWERSHARD_OFF(37),
	ATTACKMODE_IN_STANDING(38),
	NEUTRALMODE_IN_STANDING(39),
	START_LOOT(40),
	END_LOOT(41),
	START_QUESTLOOT(42),
	END_QUESTLOOT(43),
	TURN_RIGHT(44),
	TURN_LEFT(45),
	START_GLIDE(46),
	STOP_GLIDE(47),
	STOP_FLY(48),
	SUMMON_STOP_JUMP(49),
	START_FEEDING(50),
	END_FEEDING(51),
	WINDSTREAM_STRAFE(52),
	START_SPRINT(53),
	END_SPRINT(54);

	private final int id;

	EmotionType(int id) {
		this.id = id;
	}

	public int getTypeId() {
		return id;
	}

	public static EmotionType getEmotionTypeById(int id) {
		for (EmotionType emotionType : values()) {
			if (emotionType.getTypeId() == id)
				return emotionType;
		}
		return UNK;
	}

}
