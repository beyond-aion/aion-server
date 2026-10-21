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
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class CM_PET_EMOTE extends AionClientPacket {

	/**
	 * Stricter than retail, which takes any position for the pet: positions farther from the master are ignored, so the pet can't be put anywhere on
	 * the map. Pets follow within a few meters, the margin covers the master's position lagging behind on fast mounts. Remove the check to fall back
	 * to retail.
	 */
	private static final float MAX_DISTANCE_TO_MASTER = 50;
	/**
	 * Flags of the first byte: actions carry a parameter instead of a position, moves with a target carry its position too.
	 */
	private static final int ACTION_FLAG = 0x80, TARGET_FLAG = 0x04;
	private PetEmote emote;
	private int emoteId;
	private float x1, y1, z1, x2, y2, z2;
	private byte h;
	private int actionParam;

	public CM_PET_EMOTE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		emoteId = readUC();
		emote = PetEmote.getEmoteById(emoteId);
		if ((emoteId & ACTION_FLAG) != 0) {
			actionParam = readUH();
		} else {
			x1 = readF();
			y1 = readF();
			z1 = readF();
			h = readC();
			if ((emoteId & TARGET_FLAG) != 0) {
				x2 = readF();
				y2 = readF();
				z2 = readF();
			}
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Pet pet = player.getPet();

		if (pet == null || !pet.isSpawned()) // client sometimes just doesn't care...
			return;
		if (emote == PetEmote.UNKNOWN) {
			LoggerFactory.getLogger(getClass()).warn("{} / {} sent pet emote {} (actionParam: {})", player, pet, emoteId, actionParam);
			return;
		}

		if ((emoteId & ACTION_FLAG) == 0 && !PositionUtil.isInRange(player, x1, y1, z1, MAX_DISTANCE_TO_MASTER))
			return;

		switch (emote) {
			case MOVE_STOP:
			case MOVE_POSITION_UPDATE:
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false);
				break;
			case MOVETO:
				World.getInstance().updatePosition(pet, x1, y1, z1, h);
				pet.getMoveController().setNewDirection(x2, y2, z2, h);
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote), false);
				break;
			default:
				broadcastToSightedPlayers(pet, new SM_PET_EMOTE(pet, emote, actionParam), emote == PetEmote.EMOTION);
		}
	}

	private void broadcastToSightedPlayers(Pet pet, AionServerPacket packet, boolean withMaster) {
		PacketSendUtility.broadcastPacket(pet, packet, false, other -> (withMaster || !other.equals(pet.getMaster())) && other.getKnownList().sees(pet));
	}
}
