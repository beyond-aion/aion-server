package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("timeaccelerator")
public class TimeAcceleratorAI extends NpcAI {

	public TimeAcceleratorAI(Npc owner) {
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
		if (PositionUtil.isInRange(getOwner(), creature, 5) && !creature.getEffectController().hasAbnormalEffect(20727))
			AIActions.useSkill(this, 20727);
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
