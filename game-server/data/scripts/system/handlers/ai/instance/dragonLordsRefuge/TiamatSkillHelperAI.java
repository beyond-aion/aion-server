package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Estrayl March 8th, 2018
 */
@AIName("tiamat_skill_helper")
public class TiamatSkillHelperAI extends NpcAI {

	public TiamatSkillHelperAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		handleSkillTask();
	}

	private void handleSkillTask() {
		ThreadPoolManager.getInstance().schedule(() -> {
			WorldPosition p = getPosition();
			spawn(getNpcId() + 1, p.getX(), p.getY(), p.getZ(), p.getHeading());
			ThreadPoolManager.getInstance().schedule(() -> AIActions.die(this), 5000);
		}, 2000);
	}

	@Override
	protected void handleDied() {
		RespawnService.scheduleDecayTask(getOwner(), 3000);
		super.handleDied();
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
