package ai.instance.sauroBase;

import java.util.List;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

@AIName("teselik")
public class TeselikAI2 extends AggressiveNpcAI2 {

	private int stage = 0;
	private boolean isStart = false;

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		wakeUp();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		wakeUp();
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void wakeUp() {
		isStart = true;
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 90 && stage < 1) {
			stage1();
			stage = 1;
		}
	}

	private void stage1() {
		int delay = 50000;
		if (isAlreadyDead() || !isStart)
			return;
		else {
			SkillEngine.getInstance().getSkill(getOwner(), 20657, 56, getOwner()).useNoAnimationSkill();
			switch (Rnd.get(1, 2)) {
				case 1:
					random();
					break;
				case 2:
					random2();
					break;
			}
			scheduleDelayStage1(delay);
		}
	}

	private void random() {
		if (!isAlreadyDead()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead()) {
						spawn(284455, 472.12497f, 344.17401f, 181.625f, (byte) 0);
						spawn(284455, 485.1312f, 344.20688f, 181.875f, (byte) 0);
					}
				}
			}, 3000);
		}
	}

	private void random2() {
		if (!isAlreadyDead()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead()) {
						spawn(284455, 472.12497f, 328.17401f, 181.625f, (byte) 0);
						spawn(284455, 487.1312f, 327.20688f, 181.875f, (byte) 0);
					}
				}
			}, 3000);
		}
	}

	private void scheduleDelayStage1(int delay) {
		if (!isStart && !isAlreadyDead())
			return;
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					stage1();
				}
			}, delay);
		}
	}

	private void despawnNpcs(int npcId) {
		List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		despawnNpcs(284455);
		isStart = false;
		stage = 0;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		despawnNpcs(284455);
		isStart = false;
		stage = 0;
	}
}
