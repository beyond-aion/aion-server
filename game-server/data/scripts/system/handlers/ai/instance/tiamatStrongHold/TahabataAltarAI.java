package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Cheatkiller
 */
@AIName("tahabataaltar")
public class TahabataAltarAI extends NpcAI {

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(creature);
	}

	private void checkDistance(Creature creature) {
		int owner = getNpcId();
		int debuff = 0;
		switch (owner) {
			case 283116:
				debuff = 20970;
				break;
			case 283118:
				debuff = 20971;
				break;
		}
		if (creature instanceof Player) {
			if (getNpcId() == 283253 && PositionUtil.isInRangeLimited(getOwner(), creature, 25, 37) || getNpcId() == 283255
				&& PositionUtil.isInRangeLimited(getOwner(), creature, 20, 25)) {
				if (!creature.getEffectController().hasAbnormalEffect(debuff)) {
					AIActions.useSkill(this, debuff);
				}
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
