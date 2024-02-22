package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.skillengine.model.Effect;
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
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
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
			ThreadPoolManager.getInstance().schedule(() -> AIActions.die(this), 3000);
		}, 1500);
	}

	@Override
	protected void handleDied() {
		RespawnService.scheduleDecayTask(getOwner(), 3000);
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
