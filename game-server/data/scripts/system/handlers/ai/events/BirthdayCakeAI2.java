package ai.events;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.GeneralNpcAI2;

/**
 * @author Estrayl
 */
@AIName("birthday_cake")
public class BirthdayCakeAI2 extends GeneralNpcAI2 {

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		return SkillEngine.getInstance().getSkill(getOwner(), 10370, 1, player).useWithoutPropSkill();
	}
}
