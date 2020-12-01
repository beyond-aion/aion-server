package ai.events;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("birthday_cake")
public class BirthdayCakeAI extends GeneralNpcAI {

	public BirthdayCakeAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		switch (dialogActionId) {
			case SETPRO1:
				SkillEngine.getInstance().getSkill(getOwner(), 10821, 1, player).useWithoutPropSkill();
				SkillEngine.getInstance().getSkill(getOwner(), 10822, 1, player).useWithoutPropSkill();
				return true;
		}
		return false;
	}
}
