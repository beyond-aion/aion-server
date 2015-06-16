package ai.instance.darkPoeta;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;


/**
 * @author Ritsu
 *
 */

@AIName("dranalump")
public class DranaLumpAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player)
	{
		//final Creature effected = getOwner().getEffected();
		//effected.getController().cancelCurrentSkill();
	//	effected.getMoveController().abortMove();
	//	effect.setAbnormal(AbnormalState.PARALYZE.getId());
		int skillId = 0;
		int level = 0;
		switch (getNpcId()) {
			case 281178: //Drana Lump.
				skillId = 18536; //Drana Break.
				level = 46;
				break;
		}
		SkillEngine.getInstance().getSkill(player, skillId, level, player).useSkill();
		getOwner().getController().onDelete();
	}
}
