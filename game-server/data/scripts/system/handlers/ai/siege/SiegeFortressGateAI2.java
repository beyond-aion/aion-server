package ai.siege;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Request;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Source
 */
@AIName("fortressgate")
public class SiegeFortressGateAI2 extends NpcAI2 {

	private String doorName;

	@Override
	protected void handleDialogStart(Player player) {
		AI2Actions.addRequest(this, player, 160017, 0, new AI2Request() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (MathUtil.isInRange(requester, responder, 10)) {
					TeleportService2.moveToTargetWithDistance(requester, responder, PositionUtil.isBehind(requester, responder) ? 0 : 1, 3);
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
		doorName = GeoService.getInstance().getDoorName(getOwner().getWorldId(), "ab_castledoor_100.cgf", getOwner().getX(), getOwner().getY(),
			getOwner().getZ());
		if (doorName != null)
			GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, false);
		else
			LoggerFactory.getLogger(SiegeFortressGateAI2.class).warn("Couldn't find siege door name for position of " + getOwner());
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (doorName != null) {
			GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, true);
		}
	}
}
