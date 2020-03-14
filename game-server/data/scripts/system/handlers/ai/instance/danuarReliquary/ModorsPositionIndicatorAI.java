package ai.instance.danuarReliquary;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author Yeats
 */
@AIName("modors_position_indicator")
public class ModorsPositionIndicatorAI extends NpcAI {

	public ModorsPositionIndicatorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21166, 1, getOwner()).useWithoutPropSkill();

	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21166)
			getOwner().getController().delete();
	}
}
