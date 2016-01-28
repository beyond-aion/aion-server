package ai.instance.elementisForest;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author Luzien
 */
@AIName("tremorground")
public class TremoringGroundAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isUsed = new AtomicBoolean(false);

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player) {
			final Player player = (Player) creature;
			if (MathUtil.getDistance(getOwner(), player) <= 16) {
				if (isUsed.compareAndSet(false, true)) {
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							SkillEngine.getInstance().getSkill(getOwner(), 19442, 51, player).useNoAnimationSkill();
							AI2Actions.deleteOwner(TremoringGroundAI2.this);
						}

					}, 2000);
				}
			}
		}
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
}
