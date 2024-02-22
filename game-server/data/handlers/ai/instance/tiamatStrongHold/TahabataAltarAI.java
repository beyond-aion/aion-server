package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Cheatkiller
 */
@AIName("tahabataaltar")
public class TahabataAltarAI extends NpcAI {

	public TahabataAltarAI(Npc owner) {
		super(owner);
	}

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
		int debuff = switch (owner) {
			case 283116 -> 20970;
			case 283118 -> 20971;
			default -> 0;
		};
		if (creature instanceof Player) {
			if (getNpcId() == 283253 && PositionUtil.isInRangeLimited(getOwner(), creature, 25, 37)
				|| getNpcId() == 283255 && PositionUtil.isInRangeLimited(getOwner(), creature, 20, 25)) {
				if (!creature.getEffectController().hasAbnormalEffect(debuff)) {
					AIActions.useSkill(this, debuff);
				}
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
