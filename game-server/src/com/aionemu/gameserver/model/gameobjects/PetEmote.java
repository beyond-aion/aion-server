package com.aionemu.gameserver.model.gameobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ATracer
 */
public enum PetEmote {

	MOVE_STOP(0),
	MOVE_POSITION_UPDATE(8), // TODO not totally sure, couldn't reproduce. sends current coords 
	MOVETO(12),
	NO_INTERACTION(128), // sometimes after pet status action, otherwise when player is in attack mode
	FLY_START(129),
	FLY_STOP(130),
	FLY(131),
	EMOTION(133), // when stroking, etc.
	EAT_START(134), // when feeding the pet (loading bar appears)
	EAT_STOP(135), // when feeding stopped
	EAT_STOP_HEART(136), // when feeding stopped and the pet loved the food
	ATTACK_MODE_FEARLESS(140), // when player is in attack mode (some pets are of fearless nature)
	ATTACK_MODE_FEARFUL(141), // when player is in attack mode (some pets are of fearful nature)
	ALARM(142),
	ALARM_STOP_SHOUTING(144), // when starting to move while the pet is doing its shouting animation (siren will continue)
	INIT_INTERACTION(145), // init pet status action
	PERFORM_INTERACTION(146), // perform pet status action
	GET_MOOD_GIFT(147), // when player requests the present for the pet's full mood gauge
	BUFF(148),
	LOOT_START(149),
	LOOT_STOP(150),
	UNKNOWN(Integer.MAX_VALUE);

	private static final Map<Integer, PetEmote> petEmotes = new HashMap<>();

	static {
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
