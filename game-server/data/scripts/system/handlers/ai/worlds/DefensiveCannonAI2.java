package ai.worlds;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author xTz
 * @modified Neon
 */
@AIName("defensive_cannon")
public class DefensiveCannonAI2 extends ActionItemNpcAI2 {

	private AtomicBoolean canUse = new AtomicBoolean(true);

	@Override
	protected void handleUseItemFinish(Player player) {
		if (canUse.compareAndSet(true, false)) {
			switch (getNpcId()) {
				case 831338:
				case 831339:
						SkillEngine.getInstance().getSkill(getOwner(), 20364, 60, player).useNoAnimationSkill(); // Board Artillery Morph
			}
			AI2Actions.deleteOwner(this);
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
