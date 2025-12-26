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
 * @author SVDNESS
 * @version 4.8 [JDK 25]
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
			case MOVE_STOP, MOVE_POSITION_UPDATE -> {
				x1 = readF();
				y1 = readF();
				z1 = readF();
				h = readC();
			}
			case MOVETO -> {
				x1 = readF();
				y1 = readF();
				z1 = readF();
				h = readC();
				x2 = readF();
				y2 = readF();
				z2 = readF();
			}
			default -> {
				emotionId = readUC();
				unk2 = readUC();
			}
		}
	}

	@Override
	protected void runImpl() {
		final var player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		final var pet = player.getPet();
		if (pet == null || !pet.isSpawned()) {
			return;
		}
		if (emote == PetEmote.UNKNOWN) {
			LoggerFactory.getLogger(getClass()).warn("{} / {} sent pet emote {} (emotionId: {}, unk2: {})", player, pet, emoteId, emotionId, unk2);
			return;
		}
		if (x1 < 0 || y1 < 0 || z1 < 0) {
			LoggerFactory.getLogger(getClass()).warn("{} of {} sent {} at x:{}, y:{}, z:{}, h:{}.", pet, player, emote, x1, y1, z1, h);
			return;
		}
		switch (emote) {
			//case ALARM -> broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false); //TODO May be this need do!!!!
			case MOVE_STOP, MOVE_POSITION_UPDATE -> {
				if (emote == PetEmote.MOVE_POSITION_UPDATE) {
					LoggerFactory.getLogger(getClass()).warn("{} of {} sent {} at x:{}, y:{}, z:{}, h:{}", pet, player, emote, x1, y1, z1, h);
				}
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false);
			}
			case MOVETO -> {
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				pet.getMoveController().setNewDirection(x2, y2, z2, h);
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false);
			}
			default -> broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote, emotionId, unk2), emote == PetEmote.EMOTION);
		}
	}

	private void broadcastToSightedPlayers(Pet pet, AionServerPacket packet, boolean withMaster) {
		PacketSendUtility.broadcastPacket(pet, packet, false, other -> (withMaster || !other.equals(pet.getMaster())) && other.getKnownList().sees(pet));
	}
}