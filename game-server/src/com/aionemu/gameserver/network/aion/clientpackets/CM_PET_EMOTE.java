package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetEmote;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET_EMOTE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class CM_PET_EMOTE extends AionClientPacket {

	private PetEmote emote;
	private float x1, y1, z1, x2, y2, z2;
	private byte h;
	private int emotionId;
	private int unk2;

	public CM_PET_EMOTE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		emote = PetEmote.getEmoteById(readUC());
		switch (emote) {
			case MOVE_STOP:
				x1 = readF();
				y1 = readF();
				z1 = readF();
				h = readC();
				break;
			case MOVETO:
				x1 = readF();
				y1 = readF();
				z1 = readF();
				h = readC();
				x2 = readF();
				y2 = readF();
				z2 = readF();
				break;
			default:
				emotionId = readUC();
				unk2 = readUC();
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Pet pet = player.getPet();

		if (pet == null)
			return;

		// sometimes client is crazy enough to send -2.4457384E7 as z coordinate
		// TODO (check retail) either its client bug or packet problem somewhere
		// reproducible by flying randomly and falling from long height with fly resume
		if (x1 < 0 || y1 < 0 || z1 < 0)
			return;

		switch (emote) {
			case MOVE_STOP:
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				PacketSendUtility.broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote));
				break;
			case MOVETO:
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				pet.getMoveController().setNewDirection(x2, y2, z2, h);
				PacketSendUtility.broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote));
				break;
			case BUFF:
			case UNKNOWN:
				break;
			default:
				PacketSendUtility.broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote, emotionId, unk2));
		}
	}
}
