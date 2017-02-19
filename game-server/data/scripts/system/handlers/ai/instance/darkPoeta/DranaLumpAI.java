package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.ActionItemNpcAI;

/**
 * @author Ritsu
 */

@AIName("dranalump")
public class DranaLumpAI extends ActionItemNpcAI {

	@Override
	protected void handleUseItemFinish(Player player) {
		// final Creature effected = getOwner().getEffected();
		// effected.getController().cancelCurrentSkill();
		// effected.getMoveController().abortMove();
		// effect.setAbnormal(AbnormalState.PARALYZE);
		int skillId = 0;
		int level = 0;
		switch (getNpcId()) {
			case 281178: // Drana Lump.
				skillId = 18536; // Drana Break.
				level = 46;
				break;
		}
		SkillEngine.getInstance().getSkill(player, skillId, level, player).useSkill();
		getOwner().getController().delete();
	}
}
