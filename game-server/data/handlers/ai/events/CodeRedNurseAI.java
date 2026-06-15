package ai.events;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author xTz, bobobear
 */
@AIName("code_red_nurse")
public class CodeRedNurseAI extends GeneralNpcAI {

	public CodeRedNurseAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		switch (getNpcId()) {
			case 831435: // Jorpine (MON-THU)
			case 831436: // Yennu (MON-THU)
			case 831437: // Dalloren (FRI-SUN)
			case 831518: // Dalliea (FRI-SUN)
			case 831441: // Hylian (MON-THU)
			case 831442: // Rordah (MON-THU)
			case 831443: // Mazka (FRI-SUN)
			case 831524: // Desha (FRI-SUN)
				super.handleDialogStart(player);
				break;
			default:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
				break;
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		QuestEnv env = new QuestEnv(getOwner(), player, questId, dialogActionId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		if (QuestEngine.getInstance().onDialog(env) && dialogActionId != SETPRO1) {
			return true;
		}
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
			int skillId = switch (getNpcId()) {
				case 831435, 831441 -> 21280; // Jorpine / Hylian (MON-THU)
				case 831436, 831442 -> 21281; // Yennu / Rordah (MON-THU)
				case 831437, 831443 -> 21283; // Dalloren / Mazka (FRI-SUN)
				case 831518, 831524 -> 21309; // Dalliea / Desha (FRI-SUN)
				default -> 0;
			};
			if (skillId != 0)
				SkillEngine.getInstance().getSkill(getOwner(), skillId, 1, player).useWithoutPropSkill();
		} else if (dialogActionId == QUEST_SELECT && questId != 0) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10, questId));
		}
		return true;
	}
}
