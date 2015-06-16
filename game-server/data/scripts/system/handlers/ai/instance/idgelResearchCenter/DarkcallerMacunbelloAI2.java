package ai.instance.idgelResearchCenter;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;


/**
 * @author Ritsu
 * @modified Luzien
 *
 */
@AIName("darkcallermacunbello")
public class DarkcallerMacunbelloAI2 extends GeneralNpcAI2 
{

	@Override
	protected void handleSpawned() 
	{
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21121, 30, getOwner()).useWithoutPropSkill();
	}

	@Override
	protected void handleCreatureSee(Creature creature) 
	{
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature)
	{
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI2 ai, Creature creature) 
	{
		Npc sweetsteppe = getPosition().getWorldMapInstance().getNpc(230112);
		if (creature instanceof Npc)
		{
			if (sweetsteppe != null && MathUtil.isIn3dRange(getOwner(), sweetsteppe, 8) &&  sweetsteppe.getEffectController().hasAbnormalEffect(21121) )
			{
				SkillEngine.getInstance().getSkill(sweetsteppe, 21122, 30, sweetsteppe).useSkill();
				SkillEngine.getInstance().getSkill(getOwner(), 21122, 30, getOwner()).useSkill();
			}
		}
	}
}


