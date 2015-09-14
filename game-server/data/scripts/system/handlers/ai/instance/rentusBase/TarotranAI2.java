package ai.instance.rentusBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("tarotran")
public class TarotranAI2 extends AggressiveNpcAI2 {

	private List<Integer> percents = new ArrayList<Integer>();
	private AtomicBoolean isStartedEvent = new AtomicBoolean(false);
	private Future<?> eventTask;
	private Future<?> thinkTask;
	private int buffNr = 1;
	private boolean canThink = true;

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isStartedEvent.compareAndSet(false, true)) {
			spawn(282386, 383.964f, 541.48f, 147.5f, (byte) 38);
			startEventTask();
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleMoveValidate() {
		if (!isAlreadyDead() && getOwner().isSpawned()) {
			super.handleMoveValidate();
		}
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				canThink = false;
				SkillEngine.getInstance().getSkill(getOwner(), 19700, 60, getOwner()).useNoAnimationSkill();
				thinkTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						int count = Rnd.get(4, 8);
						while (count > 0) {
							count--;
							sp(282385);
						}
						canThink = true;
						Creature creature = getAggroList().getMostHated();
						if (creature == null || creature.getLifeStats().isAlreadyDead() || !getOwner().canSee(creature)) {
							setStateIfNot(AIState.FIGHT);
							think();
						} else {
							getMoveController().abortMove();
							getOwner().setTarget(creature);
							getOwner().getGameStats().renewLastAttackTime();
							getOwner().getGameStats().renewLastAttackedTime();
							getOwner().getGameStats().renewLastChangeTargetTime();
							getOwner().getGameStats().renewLastSkillTime();
							setStateIfNot(AIState.FIGHT);
							handleMoveValidate();
						}
					}

				}, 4000);
				break;
			}
		}
	}

	private void sp(int npcId) {
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(1, 2);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		final Npc npc = (Npc) spawn(npcId, p.getX() + x1, p.getY() + y1, p.getZ(), p.getHeading());
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!NpcActions.isAlreadyDead(npc) && npc.isSpawned()) {
					NpcActions.delete(npc);
				}
			}

		}, 15000);
	}

	private void startEventTask() {
		cancelEventTask();
		eventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelEventTask();
				} else {
					int skill = 0;
					switch (buffNr) {
						case 1:
							buffNr++;
							skill = 19370;
							break;
						case 2:
							buffNr++;
							skill = 19371;
							break;
						case 3:
							buffNr = 1;
							skill = 19372;
							break;
					}
					SkillEngine.getInstance().getSkill(getOwner(), skill, 60, getOwner()).useNoAnimationSkill();
				}
			}

		}, 21000, 21000);
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 85, 65, 55, 45, 30, 15 });
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private void cancelEventTask() {
		if (eventTask != null && !eventTask.isDone()) {
			eventTask.cancel(true);
		}
	}

	private void cancelThinkTask() {
		if (thinkTask != null && !thinkTask.isDone()) {
			thinkTask.cancel(true);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelEventTask();
		cancelThinkTask();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		canThink = true;
		addPercent();
		cancelEventTask();
		cancelThinkTask();
		isStartedEvent.set(false);
		removeHelpers();
		getEffectController().removeEffect(19370);
		getEffectController().removeEffect(19371);
		getEffectController().removeEffect(19372);
		super.handleBackHome();
	}

	private void removeHelpers() {
		WorldPosition p = getPosition();
		if (p != null) {
			WorldMapInstance instance = p.getWorldMapInstance();
			if (instance != null) {
				deleteNpcs(instance.getNpcs(282386));
				deleteNpcs(instance.getNpcs(282387));
				deleteNpcs(instance.getNpcs(282530));
				deleteNpcs(instance.getNpcs(282385));
			}
		}
	}

	@Override
	protected void handleDied() {
		removeHelpers();
		cancelEventTask();
		cancelThinkTask();
		super.handleDied();
	}

}
