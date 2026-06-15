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
		SkillEngine.getInstance().applyEffectDirectly(21515, getSkillLevel(), getOwner(), getOwner(), null, ForceType.DEFAULT);
		getOwner().setTarget(null);
	}

	/**
	 * Each level corresponds to 30s buff time.
	 * Retail values: extracted from npcs_abyss_monsters.xml
	 */
	private int getSkillLevel() {
		return switch (getNpcId()) {
			case 277229 -> 40; // Hangar Barricade
			case 277230 -> 50; // Ahserion's Flight Barrier
			case 277231 -> 60; // Bulwark Shield
			default -> 0;
		};
	}
}
