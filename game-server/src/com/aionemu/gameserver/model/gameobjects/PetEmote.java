package com.aionemu.gameserver.model.gameobjects;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
public enum PetEmote {

	MOVE_STOP(0),
	MOVETO(12),
	NO_INTERACTION(128), // not sure when this is sent, sometimes after miol status action
	FLY_START(129),
	FLY_STOP(130),
	FLY(131),
	EMOTION(133), // when stroking, etc.
	ALARM(142),
	INIT_INTERACTION(145), // init miol status action
	PERFORM_INTERACTION(146), // perform miol status action
	BUFF(148),
	LOOT_START(149),
	LOOT_STOP(150),
	UNKNOWN(255);

	private static TIntObjectHashMap<PetEmote> petEmotes;

	static {
		petEmotes = new TIntObjectHashMap<>();
		for (PetEmote emote : values()) {
			petEmotes.put(emote.getEmoteId(), emote);
		}
	}

	private final int emoteId;

	PetEmote(int emoteId) {
		this.emoteId = emoteId;
	}

	public int getEmoteId() {
		return emoteId;
	}

	public static PetEmote getEmoteById(int emoteId) {
		PetEmote emote = petEmotes.get(emoteId);
		return emote != null ? emote : UNKNOWN;
	}
}
