package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetEmote;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.AionServerPacket;
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
	private int emoteId, emotionId;
	private int unk2;

	public CM_PET_EMOTE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		emoteId = readUC();
		emote = PetEmote.getEmoteById(emoteId);
		switch (emote) {
			case MOVE_STOP:
			case MOVE_POSITION_UPDATE:
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

		if (pet == null || !pet.isSpawned()) // client sometimes just doesn't care...
			return;
		if (emote == PetEmote.UNKNOWN) {
			LoggerFactory.getLogger(getClass()).warn(player + " / " + pet + " sent pet emote " + emoteId + " (emotionId: " + emotionId + ", unk2: " + unk2 + ")");
			return;
		}

		// sometimes client is crazy enough to send -2.4457384E7 as z coordinate
		// TODO (check retail) either its client bug or packet problem somewhere
		// reproducible by flying randomly and falling from long height with fly resume
		if (x1 < 0 || y1 < 0 || z1 < 0) {
			LoggerFactory.getLogger(getClass()).warn(pet + " of " + player + " sent " + emote + " at x:" + x1 + ", y:" + y1 + ", z:" + z1 + ", h:" + h);
			return;
		}

		switch (emote) {
			case MOVE_STOP:
			case MOVE_POSITION_UPDATE:
				if (emote == PetEmote.MOVE_POSITION_UPDATE) { // TODO remove once we're sure "MOVE_POSITION_UPDATE" is correct and h is actually h
					LoggerFactory.getLogger(getClass()).warn(pet + " of " + player + " sent " + emote + " at x:" + x1 + ", y:" + y1 + ", z:" + z1 + ", h:" + h);
				}
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false);
				break;
			case MOVETO:
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				pet.getMoveController().setNewDirection(x2, y2, z2, h);
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false);
				break;
			default:
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote, emotionId, unk2), emote == PetEmote.EMOTION);
		}
	}

	private void broadcastToSightedPlayers(Pet pet, AionServerPacket packet, boolean withMaster) {
		PacketSendUtility.broadcastPacket(pet, packet, false, other -> (withMaster || !other.equals(pet.getMaster())) && other.getKnownList().sees(pet));
	}
}
