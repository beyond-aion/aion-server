package ai.instance.sauroBase;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;

@AIName("ranodim")
public class RanodimAI2 extends AggressiveNpcAI2 {

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
		if (hpPercentage <= 50 && stage < 1) {
			stage1();
			stage = 1;
		}
	}

	private void stage1() {
		int delay = 25000;
		if (isAlreadyDead() || !isStart)
			return;
		else {
			SkillEngine.getInstance().getSkill(getOwner(), 20702, 56, getOwner()).useNoAnimationSkill();
			scheduleDelayStage1(delay);
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

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isStart = false;
		stage = 0;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		isStart = false;
		stage = 0;
	}
}
