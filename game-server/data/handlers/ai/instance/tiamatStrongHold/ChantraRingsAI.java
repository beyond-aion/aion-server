package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("chantrarings")
public class ChantraRingsAI extends NpcAI {

	public ChantraRingsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI ai, Creature creature) {
		int debuff = getOwner().getNpcId() == 283172 ? 20735 : 20734; // 4.0
		if (creature instanceof Player) {
			if (getOwner().getNpcId() == 283172 && PositionUtil.isInRangeLimited(getOwner(), creature, 10, 18) // 4.0
				|| getOwner().getNpcId() == 283171 && PositionUtil.isInRangeLimited(getOwner(), creature, 18, 25) // 4.0
				|| getOwner().getNpcId() == 283171 && PositionUtil.isInRangeLimited(getOwner(), creature, 0, 10)) { // 4.0
				if (!creature.getEffectController().hasAbnormalEffect(debuff))
					AIActions.useSkill(this, debuff);
			}
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		despawn();
	}

	private void despawn() {
		ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 20000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
