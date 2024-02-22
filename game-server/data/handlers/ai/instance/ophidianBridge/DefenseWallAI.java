package ai.instance.ophidianBridge;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("defense_wall")
public class DefenseWallAI extends GeneralNpcAI {

	public DefenseWallAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
