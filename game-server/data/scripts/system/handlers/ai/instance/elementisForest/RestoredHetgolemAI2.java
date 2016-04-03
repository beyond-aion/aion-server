package ai.instance.elementisForest;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("restored_hetgolem")
public class RestoredHetgolemAI2 extends AggressiveNpcAI2 {

	private Future<?> lifeTask;
	private AtomicBoolean isStartEvent = new AtomicBoolean(false);

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					getMoveController().abortMove();
					setSubStateIfNot(AISubState.WALK_RANDOM);
					setStateIfNot(AIState.WALKING);
					float direction = Rnd.get(0, 199) / 100f;
					float x1 = (float) (Math.cos(Math.PI * direction) * 8);
					float y1 = (float) (Math.sin(Math.PI * direction) * 8);
					WorldPosition p = getPosition();
					if (p != null && p.getWorldMapInstance() != null) {
						getMoveController().moveToPoint(p.getX() + x1, p.getY() + y1, p.getZ());
						getOwner().setState(1);
						PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
					}
				}
			}

		}, 3000);
		startLifeTask();
	}

	private void startLifeTask() {
		lifeTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					spawnEvent();
				}
			}

		}, 5000);
	}

	private void cancelTask() {
		if (lifeTask != null && !lifeTask.isDone()) {
			lifeTask.cancel(true);
		}
	}

	private void spawnEvent() {
		if (isStartEvent.compareAndSet(false, true)) {
			WorldPosition p = getPosition();
			if (p != null && p.getWorldMapInstance() != null) {
				spawn(282308, p.getX(), p.getY(), p.getZ(), p.getHeading());
				Npc npc = (Npc) spawn(282465, p.getX(), p.getY(), p.getZ(), p.getHeading());
				NpcActions.delete(npc);
			}
			AI2Actions.deleteOwner(this);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		cancelTask();
		spawnEvent();
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case CAN_RESIST_ABNORMAL:
				return true;
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
