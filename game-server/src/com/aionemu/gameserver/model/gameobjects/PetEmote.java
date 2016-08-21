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
	EMOTION(133), // when stroking, etc.
	INIT_INTERACTION(145), // init miol status action
	PERFORM_INTERACTION(146), // perform miol status action
	ALARM(-114),
	UNK_M110(-110),
	UNK_M111(-111),
	UNK_M123(-123),
	FLY(-125),
	UNK_M128(-128),
	UNKNOWN(255);

	private static TIntObjectHashMap<PetEmote> petEmotes;

	static {
		petEmotes = new TIntObjectHashMap<>();
		for (PetEmote emote : values()) {
			petEmotes.put(emote.getEmoteId(), emote);
		}
	}

	private int emoteId;

	private PetEmote(int emoteId) {
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
