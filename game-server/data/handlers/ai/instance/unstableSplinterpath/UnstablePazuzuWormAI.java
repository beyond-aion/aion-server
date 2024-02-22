package ai.instance.unstableSplinterpath;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("unstablepazuzuworm")
public class UnstablePazuzuWormAI extends AggressiveNpcAI {

	public UnstablePazuzuWormAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			AIActions.targetCreature(UnstablePazuzuWormAI.this, getPosition().getWorldMapInstance().getNpc(219554));
			AIActions.useSkill(UnstablePazuzuWormAI.this, 19291);
		}, 3000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
