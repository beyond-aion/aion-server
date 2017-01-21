package ai.instance.rentusBase;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author xTz
 */
@AIName("collapsed_reian_building")
public class CollapsedReianBuildingAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 20088, 60, getOwner()).useNoAnimationSkill();
	}

}
