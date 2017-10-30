package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.NoActionAI;

/**
 * @author Yeats
 */
@AIName("ahserion_gate")
public class AhserionGate extends NoActionAI {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		useBuff();
	}

	private void useBuff() {
		switch (getNpcId()) {
			case 277229:
				SkillEngine.getInstance().applyEffectDirectly(21515, 50, getOwner(), getOwner(), 0);
				break;
			case 277230:
				SkillEngine.getInstance().applyEffectDirectly(21515, 60, getOwner(), getOwner(), 0);
				break;
			case 277231:
				SkillEngine.getInstance().applyEffectDirectly(21515, 70, getOwner(), getOwner(), 0);
				break;
		}
		getOwner().setTarget(null);
	}

	@Override
	protected void handleDied() {
		RespawnService.scheduleDecayTask(getOwner(), 5000);
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
