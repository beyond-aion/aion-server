package ai.siege;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Source
 */
@AIName("fortressgate")
public class SiegeFortressGateAI extends NpcAI {

	private String doorName = null;

	@Override
	protected void handleDialogStart(Player player) {
		AIActions.addRequest(this, player, 160017, 0, new AIRequest() {

			@Override
			public void acceptRequest(Creature fortressGate, Player responder, int requestId) {
				if (PositionUtil.isInTalkRange(responder, (Npc) fortressGate)) {
					TeleportService.moveToTargetWithDistance(fortressGate, responder, PositionUtil.isBehind(responder, fortressGate) ? 0 : 1, 3);
				} else {
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_FAR_FROM_NPC());
				}
			}
		});
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
				return false;
			default:
				return super.ask(question);
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (GeoDataConfig.GEO_DOORS_ENABLE) {
			doorName = GeoService.getInstance().getDoorName(getOwner().getWorldId(), "ab_castledoor_100.cgf", getOwner().getX(), getOwner().getY(),
				getOwner().getZ());
			if (doorName != null)
				GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, false);
			else
				LoggerFactory.getLogger(SiegeFortressGateAI.class).warn("Couldn't find siege door name for position of " + getOwner());
		}
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (doorName != null)
			GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, true);
	}
}
