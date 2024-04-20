package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetEmote;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, Neon
 */
public class SM_PET_EMOTE extends AionServerPacket {

	private Pet pet;
	private PetEmote emote;
	private int emotionId, param1;

	public SM_PET_EMOTE(Pet pet, PetEmote emote) {
		this(pet, emote, 0, 0);
	}

	public SM_PET_EMOTE(Pet pet, PetEmote emote, int emotionId, int param1) {
		this.pet = pet;
		this.emote = emote;
		this.emotionId = emotionId;
		this.param1 = param1;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(pet.getObjectId());
		writeC(emote.getEmoteId());
		switch (emote) {
			case MOVE_STOP:
				writeF(pet.getX());
				writeF(pet.getY());
				writeF(pet.getZ());
				writeC(pet.getHeading());
				break;
			case MOVETO:
				writeF(pet.getX());
				writeF(pet.getY());
				writeF(pet.getZ());
				writeC(pet.getHeading());
				writeF(pet.getMoveController().getTargetX2());
				writeF(pet.getMoveController().getTargetY2());
				writeF(pet.getMoveController().getTargetZ2());
				break;
			default:
				writeC(emotionId);
				writeC(param1); // happinessAdded?
				break;
		}
	}
}
