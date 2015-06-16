package ai.instance.idgelResearchCenter;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;


/**
 * @author Ritsu
 *
 */
@AIName("retributive_effigy")
public class RetributiveEffigyAI2 extends AggressiveNpcAI2
{

	@Override
	protected void handleSpawned() 
	{
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21124, 60, getOwner()).useWithoutPropSkill();
	}
}
