package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author ATracer, orz, KID
 */
public class CM_TELEPORT_SELECT extends AionClientPacket {

	/**
	 * NPC ID
	 */
	public int targetObjectId;

	/**
	 * Destination of teleport
	 */
	public int locId;

	public CM_TELEPORT_SELECT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
		locId = readD(); // locationId
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getLifeStats().isAlreadyDead())
			return;

		AionObject obj = player.getKnownList().getObject(targetObjectId);
		if (obj != null && obj instanceof Npc) {
			Npc npc = (Npc) obj;
			int npcId = npc.getNpcId();
			if (!MathUtil.isInRange(npc, player, npc.getObjectTemplate().getTalkDistance() + 2)) {
				return;
			}
			TeleporterTemplate teleport = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npcId);
			if (teleport != null) {
				TeleportService2.teleport(teleport, locId, player, npc, npc.getName().toLowerCase().contains("statue") ? TeleportAnimation.JUMP_IN_STATUE : TeleportAnimation.JUMP_IN);
			} else
				LoggerFactory.getLogger(CM_TELEPORT_SELECT.class).warn("teleportation id " + locId + " was not found on npc " + npcId);
		} else
			LoggerFactory.getLogger(CM_TELEPORT_SELECT.class).debug(
				"player " + player.getName() + " requested npc " + targetObjectId + " for teleportation " + locId
					+ ", but he doesnt have such npc in knownlist");
	}
}
