package ai.worlds;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author xTz
 */
@AIName("defensive_cannon")
public class DefensiveCannonAI2 extends ActionItemNpcAI2 {

	private AtomicBoolean canUse = new AtomicBoolean(true);

	@Override
	protected void handleUseItemFinish(Player player) {

		if (canUse.compareAndSet(true, false)) {
			int morphSkill = getMorphSkill();
			SkillEngine.getInstance().getSkill(getOwner(), morphSkill >> 8, morphSkill & 0xFF, player).useNoAnimationSkill();
			AI2Actions.deleteOwner(this);
		}
	}

	private int getMorphSkill() {
		switch (getNpcId()) {
			case 831339:
				return 0x4F8D3C;
			case 831338:
				return 0x4F8C3C;

		}
		return 0;
	}

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			default:
				return super.pollInstance(question);
		}
	}
}