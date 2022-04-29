package ai.instance.nightmareCircus;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("enragednightmare")
public class EnragedNightmareAI extends AggressiveNpcAI {

	private Future<?> skillTask;
	private Npc boss;

	public EnragedNightmareAI(Npc owner) {
		super(owner);
	}

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
			getOwner().getAi().setStateIfNot(AIState.WALKING);
			WalkManager.startWalking((NpcAI) getOwner().getAi());
			getOwner().setState(CreatureState.ACTIVE, true);
			getOwner().getMoveController().moveToPoint(521.60913f, 551.00684f, 198.75f);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		} else {
			getOwner().getAi().setStateIfNot(AIState.WALKING);
			WalkManager.startWalking((NpcAI) getOwner().getAi());
			getOwner().setState(CreatureState.ACTIVE, true);
			getOwner().getMoveController().moveToPoint(525.3831f, 585.3808f, 199.0476f);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		}

	}

	private void useSkill(NpcAI ai, Creature creature) {
		boss = getPosition().getWorldMapInstance().getNpc(233467);
		if (boss != null && !boss.isDead()) {
			if (boss.getLifeStats().getHpPercentage() < 100) {
				EmoteManager.emoteStopAttacking(getOwner());
				SkillEngine.getInstance().getSkill(getOwner(), 21342, 1, boss).useSkill();
				if (getOwner().getController().useSkill(21342)) {
					skillTask = ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(EnragedNightmareAI.this), 3000);
				}
			}
		}
	}

	@Override
	protected void handleBackHome() {

	}
}
