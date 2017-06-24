package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("gravitycrusher")
public class GravityCrusherAI extends AggressiveNpcAI {

	private Future<?> skillTask;
	private Future<?> transformTask;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final WorldMapInstance instance = getPosition().getWorldMapInstance();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.targetCreature(GravityCrusherAI.this, Rnd.get(instance.getPlayersInside()));
				setStateIfNot(AIState.WALKING);
				getOwner().setState(CreatureState.ACTIVE, true);
				getMoveController().moveToTargetObject();
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
				transform();
			}
		}, 2000);
	}

	private void transform() {
		transformTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					if (skillTask != null)
						skillTask.cancel(true);
					AIActions.useSkill(GravityCrusherAI.this, 20967); // self destruct

					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							PacketSendUtility.broadcastToMap(getOwner(), 1401554);
							spawn(283140, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
							AIActions.deleteOwner(GravityCrusherAI.this);
						}
					}, 3000);

				}
			}
		}, 30000);
	}

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (skillTask != null)
			return;
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(GravityCrusherAI.this, 20987);
			}
		}, 0, 5000);
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled())
			skillTask.cancel(true);
		if (transformTask != null && !transformTask.isCancelled())
			transformTask.cancel(true);
	}

	@Override
	public void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	public void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	public boolean canThink() {
		return false;
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
