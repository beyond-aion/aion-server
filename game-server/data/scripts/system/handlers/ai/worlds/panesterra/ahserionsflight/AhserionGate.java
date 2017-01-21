package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Yeats
 *
 */
@AIName("ahserion_gate")
public class AhserionGate extends NpcAI {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		useBuff();
	}
	
	private void useBuff() {
		switch (getOwner().getNpcId()) {
			case 277229:
				SkillEngine.getInstance().applyEffectDirectly(21515, 30, getOwner(), getOwner(), 0);
				break;
			case 277230:
				SkillEngine.getInstance().applyEffectDirectly(21515, 40, getOwner(), getOwner(), 0);
				break;
			case 277231:
				SkillEngine.getInstance().applyEffectDirectly(21515, 50, getOwner(), getOwner(), 0);
				break;
		}
		getOwner().setTarget(null);
	}
}
