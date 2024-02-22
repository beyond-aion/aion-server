package ai.instance.aturamSkyFortress;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("shulack_thermo_bomb")
public class ShulackThermoBombAI extends AggressiveNpcAI {

	public ShulackThermoBombAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		doSchedule();
	}

	private void doSchedule() {

		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 19416, 49, getOwner()).useNoAnimationSkill();
				ThreadPoolManager.getInstance().schedule(() -> {
					if (!isDead())
						despawn();
				}, 4000);
			}
		}, 2000);

	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	private void despawn() {
		AIActions.deleteOwner(this);
	}

}
