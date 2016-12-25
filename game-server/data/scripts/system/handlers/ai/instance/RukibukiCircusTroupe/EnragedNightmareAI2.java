package ai.instance.RukibukiCircusTroupe;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Ritsu
 */
@AIName("enragednightmare")
public class EnragedNightmareAI2 extends AggressiveNpcAI2 {

	private Future<?> skillTask;
	private Npc boss;

	@Override
	protected void handleCreatureSee(Creature creature) {
		useSkill(this, boss);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		useSkill(this, boss);
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();

		if (getOwner().getPosition().getX() == 521.585f && getOwner().getPosition().getY() == 510.16528f) {
			((AbstractAI) getOwner().getAi2()).setStateIfNot(AIState.WALKING);
			WalkManager.startWalking((NpcAI2) getOwner().getAi2());
			getOwner().setState(CreatureState.ACTIVE, true);
			getOwner().getMoveController().moveToPoint(521.60913f, 551.00684f, 198.75f);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		} else {
			((AbstractAI) getOwner().getAi2()).setStateIfNot(AIState.WALKING);
			WalkManager.startWalking((NpcAI2) getOwner().getAi2());
			getOwner().setState(CreatureState.ACTIVE, true);
			getOwner().getMoveController().moveToPoint(525.3831f, 585.3808f, 199.0476f);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		}

	}

	private void useSkill(NpcAI2 ai, Creature creature) {
		boss = getPosition().getWorldMapInstance().getNpc(233467);
		if (boss != null && !NpcActions.isAlreadyDead(boss)) {
			if (boss.getLifeStats().getHpPercentage() < 100) {
				EmoteManager.emoteStopAttacking(getOwner());
				SkillEngine.getInstance().getSkill(getOwner(), 21342, 1, boss).useSkill();
				if (getOwner().getController().useSkill(21342)) {
					skillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							AI2Actions.deleteOwner(EnragedNightmareAI2.this);
						}
					}, 3000);
				}
			}
		}
	}

	@Override
	protected void handleBackHome() {

	}
}
