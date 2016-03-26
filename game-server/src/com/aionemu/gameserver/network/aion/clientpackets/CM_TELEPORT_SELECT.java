package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, orz, KID
 */
public class CM_TELEPORT_SELECT extends AionClientPacket {

	/**
	 * NPC object ID
	 */
	public int targetObjId;

	/**
	 * Destination of teleport
	 */
	public int locId;

	public CM_TELEPORT_SELECT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjId = readD();
		locId = readD(); // locationId
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getLifeStats().isAlreadyDead())
			return;

		AionObject obj = player.getKnownList().getObject(targetObjId);
		if (!(obj instanceof Npc)) {
			AuditLogger.info(player,
				"Tried to teleport via " + (obj == null ? "unknown npc" : obj.getClass().getSimpleName()) + " object id: " + targetObjId);
			return;
		}

		Npc npc = (Npc) obj;
		int npcId = npc.getNpcId();
		if (!MathUtil.isInRange(npc, player, npc.getObjectTemplate().getTalkDistance() + 1, false)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_FAR_FROM_NPC());
			return;
		}
		TeleporterTemplate teleport = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npcId);
		if (teleport != null)
			TeleportService2.teleport(teleport, locId, player, npc,
				npc.getName().toLowerCase().contains("statue") ? TeleportAnimation.JUMP_IN_STATUE : TeleportAnimation.JUMP_IN);
		else
			AuditLogger.info(player, "Tried to teleport via npc " + npcId + " but he has no teleporter template.");
	}
}
