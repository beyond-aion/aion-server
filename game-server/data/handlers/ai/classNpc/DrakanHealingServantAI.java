package ai.classNpc;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("drakanhealingservant")
public class DrakanHealingServantAI extends NpcAI {

	public DrakanHealingServantAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			if (getCreator() == null)
				return;
			AIActions.targetCreature(DrakanHealingServantAI.this, (Creature) getCreator());
			heal();
		}, 2000);
	}

	private void heal() {
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> getOwner().getController().useSkill(20520), 1000, 6000);
		getOwner().getController().addTask(TaskId.SKILL_USE, task);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
