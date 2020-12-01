package ai;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Cheatkiller, Bobobear
 */
@AIName("invisible_kisk")
public class InvisiblekiskAI extends KiskAI {

	public InvisiblekiskAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		AIActions.useSkill(this, getHideSkillId());
	}

	private int getHideSkillId() {
		switch (getNpcId()) {
			case 701768:
			case 701770:
				return 21261;
			case 701769:
			case 701771:
				return 21262;
		}
		return 0;
	}
}
