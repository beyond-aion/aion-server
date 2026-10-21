package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author alexa026, corrected by Metos, ATracer
 */
public class CM_START_LOOT extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_START_LOOT.class);
	/**
	 * Allowed distance between the bounds of the player and the corpse.
	 */
	private static final float LOOT_DISTANCE = 6.5f;
	/**
	 * Height up to which a corpse above the player only counts with its horizontal distance, so bosses dying on ledges can be looted from below.
	 */
	private static final float MAX_HEIGHT_ABOVE_PLAYER = 50;
	/**
	 * Horizontal distance from which corpses can be looted in the Danuar Reliquary, whose boss dies on top of the pillars.
	 */
	private static final float RELIQUARY_LOOT_DISTANCE = 30;
	private static final Set<Integer> RELIQUARY_MAPS = Set.of(WorldMapType.DANUAR_RELIQUARY.getId(), WorldMapType.LUCKY_DANUAR_RELIQUARY.getId(),
		WorldMapType.INFERNAL_DANUAR_RELIQUARY.getId());
	/**
	 * Object id of the corpse whose loot list is opened or closed
	 */
	private int targetObjectId;
	private Action action;

	public CM_START_LOOT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
		int actionId = readUC();
		action = Action.getById(actionId);
		if (action == null)
			log.warn("{} sent unknown loot action type {}", getConnection().getActivePlayer(), actionId);
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (action == null)
			return;
		switch (action) {
			case OPEN -> {
				if (!(player.getKnownList().getObject(targetObjectId) instanceof Npc npc) || !isInLootRange(player, npc)) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_LOOT_FAIL_TOO_FAR());
					return;
				}
				DropService.getInstance().requestDropList(player, targetObjectId);
			}
			case CLOSE -> DropService.getInstance().closeDropList(player, targetObjectId);
		}
	}

	private boolean isInLootRange(Player player, Npc npc) {
		if (player.getWorldId() != npc.getWorldId() || player.getInstanceId() != npc.getInstanceId())
			return false;
		float range = LOOT_DISTANCE + player.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide()
			+ npc.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide();
		float dx = npc.getX() - player.getX();
		float dy = npc.getY() - player.getY();
		float dz = npc.getZ() - player.getZ();
		float horizontal = dx * dx + dy * dy;
		if (horizontal + dz * dz < range * range)
			return true;
		if (dz > 0 && dz < MAX_HEIGHT_ABOVE_PLAYER && horizontal < range * range)
			return true;
		return RELIQUARY_MAPS.contains(player.getWorldId()) && horizontal < RELIQUARY_LOOT_DISTANCE * RELIQUARY_LOOT_DISTANCE;
	}

	private enum Action {

		OPEN(0),
		CLOSE(1);

		private final int id;

		Action(int id) {
			this.id = id;
		}

		static Action getById(int id) {
			for (Action value : values())
				if (value.id == id)
					return value;
			return null;
		}
	}
}
