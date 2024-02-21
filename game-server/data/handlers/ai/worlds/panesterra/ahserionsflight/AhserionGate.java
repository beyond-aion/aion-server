package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;

import ai.NoActionAI;

/**
 * @author Yeats
 */
@AIName("ahserion_gate")
public class AhserionGate extends NoActionAI {

	public AhserionGate(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		useBuff();
	}

	private void useBuff() {
		SkillEngine.getInstance().applyEffectDirectly(21515, getNpcId() - 277227, getOwner(), getOwner(), null, ForceType.DEFAULT);
		getOwner().setTarget(null);
	}

	@Override
	protected void handleDied() {
		RespawnService.scheduleDecayTask(getOwner(), 5000);
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case LOOT -> false;
			default -> super.ask(question);
		};
	}
}
