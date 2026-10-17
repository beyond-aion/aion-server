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
	private int actionParam;

	public SM_PET_EMOTE(Pet pet, PetEmote emote) {
		this(pet, emote, 0);
	}

	public SM_PET_EMOTE(Pet pet, PetEmote emote, int actionParam) {
		this.pet = pet;
		this.emote = emote;
		this.actionParam = actionParam;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(pet.getObjectId());
		writeC(emote.getEmoteId());
		switch (emote) {
			case MOVE_STOP:
			case MOVE_POSITION_UPDATE:
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
				writeH(actionParam);
				break;
		}
	}
}
