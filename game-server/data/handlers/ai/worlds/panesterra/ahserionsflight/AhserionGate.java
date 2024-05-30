package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;

/**
 * @author Yeats
 */
@AIName("ahserion_gate")
public class AhserionGate extends AhserionConstructAI {

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
}
