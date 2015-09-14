/*
 * Test Instrumen Dainatum
 * The Illuminary Obelisk 4.5
 */
package ai.instance.illuminaryObelisk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author M.O.G. Dision
 */

@AIName("dainatum")
public class TestInstrumentDainatumAI2 extends AggressiveNpcAI2 {

	private List<Integer> percents = new ArrayList<Integer>();
	private Future<?> TimerTasks;
	protected NpcAI2 ai2;
	private boolean isCancelled;

	@Override
	protected void handleSpawned() {
		addPercent();
		super.handleSpawned();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage > 95 && percents.size() < 6) {
			addPercent();
		}

		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 95:
						BombTimer();
						break;
					case 70:
						Boss_Portal_Destroy();
						break;
					case 50:
						spawnHealers();
						break;
					case 30:
						spawnSupport();
						break;
					case 20:
						spawnHealers();
						break;
					case 10:
						spawnSupport();
						break;
				}
				percents.remove(percent);
				break;
			}
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 95, 70, 30, 20, 10 });
	}

	private void Boss_Timer_01() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402143);
	}

	private void Boss_Timer_02() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402144);
	}

	private void Boss_Timer_03() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402146);
	}

	private void Boss_Portal_Destroy() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402212);
		Npc Portal = getPosition().getWorldMapInstance().getNpc(702216);
		if (Portal != null) {
			Portal.getController().onDelete();
		}
	}

	private void DainatumDestroy(int skillId) {
		SkillEngine.getInstance().getSkill(getOwner(), skillId, 65, getOwner()).useNoAnimationSkill();
	}

	private void BombTimer() {

		TimerTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead() && isCancelled == true) {
					CancelTask();
				}
				else {
					Boss_Timer_01();
				}
			}
		}, 1000);

		TimerTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead() && isCancelled == true) {
					CancelTask();
				}
				else {
					Boss_Timer_02();
				}
			}
		}, 60000);

		TimerTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Boss_Timer_03();
				DainatumDestroy(21275);
			}
		}, 360000);

		TimerTasks = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead() && isCancelled == true) {
					CancelTask();
				}
				else {
					DespawnDainatum();
				}
			}
		}, 365000);
	}

	private void RespawnOwner() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.scheduleRespawn(ai2);
			}
		}, 1800000);

	}

	private void DespawnDainatum() {
		Npc npc = getOwner();
		NpcActions.delete(npc);
		CancelTask();
		isCancelled = true;
		RespawnOwner();
	}

	private void CancelTask() {
		if (TimerTasks != null && !TimerTasks.isCancelled()) {
			TimerTasks.cancel(true);
		}
	}

	private void spawnSupport() {
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(0, 10);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		spawn(284859, p.getX() + x1, p.getY() + y1, p.getZ(), p.getHeading());
		spawn(284859, p.getX() + x1, p.getY() + y1, p.getZ(), p.getHeading());
		spawn(284860, p.getX() + x1, p.getY() + y1, p.getZ(), p.getHeading());
		spawn(284860, p.getX() + x1, p.getY() + y1, p.getZ(), p.getHeading());
	}

	private void spawnHealers() {
		spawn(284861, 265.34f, 254.80f, 455.12f, (byte) 60);
		spawn(284861, 255.59f, 264.44f, 455.12f, (byte) 89);
		spawn(284861, 248.42f, 247.52f, 455.12f, (byte) 15);
	}
	
	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private List<Npc> getNpcs(int npcId) {
		return getPosition().getWorldMapInstance().getNpcs(npcId);
	}

	@Override
	protected void handleDespawned() {
		percents.clear();
		CancelTask();
		isCancelled = true;
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		CancelTask();
		isCancelled = true;
		super.handleDied();
		deleteNpcs(getNpcs(284861));
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		super.handleBackHome();
	}

}
