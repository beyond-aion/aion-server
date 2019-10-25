package ai.siege;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Source
 */
@AIName("fortressgate")
public class FortressGateAI extends NpcAI {

	public FortressGateAI(Npc owner) {
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

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().setDoorState(getOwner().getInstanceId(), false);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		getOwner().setDoorState(getOwner().getInstanceId(), true);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		getOwner().setDoorState(getOwner().getInstanceId(), true);
	}
}
