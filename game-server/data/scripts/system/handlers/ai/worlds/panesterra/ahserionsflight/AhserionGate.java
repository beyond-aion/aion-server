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
		switch (getNpcId()) {
			case 277229:
				SkillEngine.getInstance().applyEffectDirectly(21515, 50, getOwner(), getOwner(), null, ForceType.DEFAULT);
				break;
			case 277230:
				SkillEngine.getInstance().applyEffectDirectly(21515, 60, getOwner(), getOwner(), null, ForceType.DEFAULT);
				break;
			case 277231:
				SkillEngine.getInstance().applyEffectDirectly(21515, 70, getOwner(), getOwner(), null, ForceType.DEFAULT);
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
