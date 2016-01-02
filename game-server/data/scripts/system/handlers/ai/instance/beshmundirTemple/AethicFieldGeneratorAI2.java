package ai.instance.beshmundirTemple;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastTable;
import ai.GeneralNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
@AIName("aethic_field_generator")
public class AethicFieldGeneratorAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isAggred = new AtomicBoolean(false);
	private Future<?> aggroTask;
	private List<Integer> percents = new FastTable<Integer>();

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 19127, 1, getOwner()).useNoAnimationSkill();
		addPercent();
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (isAggred.compareAndSet(false, true)) {
			getPosition().getWorldMapInstance().getDoors().get(471).setOpen(false);
			aggroTask();
		}
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 75:
						spawn(216532, 1364.3739f, 88.38297f, 248.39449f, (byte) 37);
						spawn(216532, 1349.2109f, 87.81685f, 248.37459f, (byte) 5);
						break;
					case 50:
						spawn(216532, 1362.2529f, 88.02893f, 248.40222f, (byte) 53);
						spawn(216532, 1352.1556f, 87.69477f, 248.38733f, (byte) 4);
						spawn(216532, 1356.2352f, 90.32507f, 247.75319f, (byte) 92);
						break;
					case 25:
						spawn(216532, 1366.1492f, 84.480515f, 248.59457f, (byte) 51);
						spawn(216532, 1349.3264f, 83.13163f, 248.59457f, (byte) 6);
						spawn(216532, 1354.3655f, 85.42517f, 248.59457f, (byte) 27);
						spawn(216532, 1360.4175f, 86.17866f, 248.59457f, (byte) 34);
						break;
				}
				break;
			}
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 75, 50, 25 });
	}

	private void aggroTask() {
		aggroTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelAggroTask();
				} else {
					if (!isInRangePlayer()) {
						handleBackHome();
						getLifeStats().triggerRestoreTask();
					}
				}
			}

		}, 2000, 2000);
	}

	private boolean isInRangePlayer() {
		for (Player player : getKnownList().getKnownPlayers().values()) {
			if (isInRange(player, 40) && !PlayerActions.isAlreadyDead(player) && getOwner().canSee(player)) {
				return true;
			}
		}
		return false;
	}

	private void cancelAggroTask() {
		if (aggroTask != null && !aggroTask.isDone()) {
			aggroTask.cancel(true);
		}
	}

	@Override
	protected void handleBackHome() {
		handleFinishAttack();
		cancelAggroTask();
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			instance.getDoors().get(471).setOpen(true);
			deleteNpcs(instance.getNpcs(216532));
		}
		isAggred.set(false);
		addPercent();
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			instance.getDoors().get(471).setOpen(true);
			deleteNpcs(instance.getNpcs(216532));
		}
		cancelAggroTask();
		super.handleDied();

	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}
}
