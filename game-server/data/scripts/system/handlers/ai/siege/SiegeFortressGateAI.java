package ai.siege;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.siege.Siege;
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

	public SiegeFortressGateAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_ASK_PASS_BY_GATE, new AIRequest() {

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
			case SHOULD_LOOT:
			case SHOULD_RESPAWN:
				return false;
			default:
				return super.ask(question);
		}
	}

	private String getMeshFileName() {
		switch (getOwner().getWorldId()) {
			case 600090000:
				switch (getNpcId()) {
					case 252115:
					case 252116:
					case 252117:
						return "ldf5_fortress_door_01.cgf";
					case 881578:
						return "barricade_light_large_01a.cgf";
					case 881579:
						return "barricade_vritra_large_01a.cgf";
					case 881580:
						return "barricade_dark_large_01a.cgf";
				}
				break;
			default:
				return "ab_castledoor_100.cgf";
		}
		return "";
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		doorName = GeoService.getInstance().getSiegeDoorName(getOwner().getWorldId(), getMeshFileName(), getOwner().getX(), getOwner().getY(),
			getOwner().getZ());
		updateDoorState(false, true);
	}

	@Override
	protected void handleDied() {
		if (getOwner() instanceof SiegeNpc) {
			Siege<?> siege = SiegeService.getInstance().getSiege(((SiegeNpc) getOwner()).getSiegeId());
			if (siege != null) {
				SiegeNpc boss = siege.getBoss();
				if (boss != null)
					boss.getEffectController().removeEffect(19111);
			}
		}
		super.handleDied();
		updateDoorState(true, false);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		updateDoorState(true, false);
	}

	private void updateDoorState(boolean isOpened, boolean shouldLog) {
		if (doorName != null)
			GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, isOpened);
		else if (shouldLog)
			LoggerFactory.getLogger(SiegeFortressGateAI.class).warn("Couldn't find siege door name for position of " + getOwner());
	}
}
