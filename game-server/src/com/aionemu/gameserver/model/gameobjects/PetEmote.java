package com.aionemu.gameserver.model.gameobjects;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
public enum PetEmote {

	MOVE_STOP(0),
	MOVETO(12),
	ALARM(-114),
	UNK_M110(-110),
	UNK_M111(-111),
	UNK_M123(-123),
	FLY(-125),
	UNK_M128(-128),
	UNKNOWN(255);

	private static TIntObjectHashMap<PetEmote> petEmotes;

	static {
		petEmotes = new TIntObjectHashMap<PetEmote>();
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
