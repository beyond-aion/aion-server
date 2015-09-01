package ai.instance.idgelResearchCenter;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;


/**
 * @author Ritsu
 *
 */
@AIName("unmaker")
public class UnmakerAI2 extends AggressiveNpcAI2
{
	
	@Override
	protected void handleSpawned() 
	{
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21121, 30, getOwner()).useWithoutPropSkill();
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{

			@Override
			public void run() 
			{
				 getOwner().getLifeStats().increaseHp(getOwner().getLifeStats().getMaxHp() + 1);
			}
		}, 10000);
	}
	
	
	@Override
	protected void handleAttack(Creature creature) 
	{
		super.handleAttack(creature);
	}
	
}
