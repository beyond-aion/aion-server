package ai.instance.muadasTrencher;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 *
 * @author xTz
 */
@AIName("sandy_muck")
public class SandyMuckAI2 extends AggressiveNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useSkill(500);
		useSkill(5000);
		useSkill(10000);
		useSkill(15000);
		useSkill(20000);
		useSkill(25000);
		useSkill(30000);
	}

	private void useSkill(final int time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					if (time != 30000) {
						SkillEngine.getInstance().getSkill(getOwner(), 19900, 50, getOwner()).useNoAnimationSkill();
					}
					else {
						AI2Actions.deleteOwner(SandyMuckAI2.this);
					}
				}
			}

		}, time);
	}

	@Override
	public AIAnswer ask(AIQuestion question) {
		switch (question) {
			case CAN_ATTACK_PLAYER:
				return AIAnswers.POSITIVE;
			default:
				return AIAnswers.NEGATIVE;
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		AI2Actions.deleteOwner(this);
	}

}