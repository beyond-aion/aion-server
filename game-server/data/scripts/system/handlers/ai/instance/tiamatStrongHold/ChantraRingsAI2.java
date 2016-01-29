package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author Cheatkiller
 */
@AIName("chantrarings")
public class ChantraRingsAI2 extends NpcAI2 {

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI2 ai, Creature creature) {
		int debuff = getOwner().getNpcId() == 283172 ? 20735 : 20734; // 4.0
		if (creature instanceof Player) {
			if (getOwner().getNpcId() == 283172 && MathUtil.isIn3dRangeLimited(getOwner(), creature, 10, 18) // 4.0
				|| getOwner().getNpcId() == 283171 && MathUtil.isIn3dRangeLimited(getOwner(), creature, 18, 25) // 4.0
				|| getOwner().getNpcId() == 283171 && MathUtil.isIn3dRangeLimited(getOwner(), creature, 0, 10)) { // 4.0
				if (!creature.getEffectController().hasAbnormalEffect(debuff))
					AI2Actions.useSkill(this, debuff);
			}
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		despawn();
	}

	private void despawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getOwner().getController().onDelete();
			}
		}, 20000);
	}

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
}
