package ai.instance.elementisForest;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien
 */
@AIName("terma")
public class TrappedTermaAI2 extends NpcAI2 {

	private boolean isMove;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (!isMove) {
			isMove = true;
			moveToDead();
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		Npc freeTerma = (Npc) spawn(205495, 455.94f, 537.06f, 132.6f, (byte) 0);
		spawn(701009, 451.706f, 534.313f, 131.979f, (byte) 0);
		PacketSendUtility.broadcastMessage(freeTerma, 1500444, 3000);
	}

	private void moveToDead() {
		((AbstractAI) getOwner().getAi2()).setStateIfNot(AIState.WALKING);
		WalkManager.startWalking((NpcAI2) getOwner().getAi2());
		getOwner().setState(1);
		getOwner().getMoveController().moveToPoint(455.93f, 537.2f, 132.55f);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		dead();
	}

	private void dead() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getOwner().getController().die();
			}

		}, 16000);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
