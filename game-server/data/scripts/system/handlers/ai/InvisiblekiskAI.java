package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Cheatkiller, modified Bobobear
 */
@AIName("invisiblekisk")
public class InvisiblekiskAI extends KiskAI {

	public InvisiblekiskAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int skill = getOwner().getTribe().equals(TribeClass.GENERAL_DARK) ? 21262 : 21261;
		SkillEngine.getInstance().getSkill(getOwner(), skill, 60, getOwner()).useNoAnimationSkill();
	}
}
