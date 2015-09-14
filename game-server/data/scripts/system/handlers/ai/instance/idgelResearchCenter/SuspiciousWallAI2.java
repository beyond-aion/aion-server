package ai.instance.idgelResearchCenter;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Ritsu
 * @rework Luzien
 */
@AIName("suspiciouswall")
public class SuspiciousWallAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleDied() {
		SkillEngine.getInstance().getSkill(getOwner(), 20784, 1, getOwner()).useWithoutPropSkill();
		super.handleDied();
	}
}
