package ai.instance.nightmareCircus;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * FIXME NPC should only be visible to the owner (implement enum SummonOwner)
 *
 * @author Ritsu
 */
@AIName("nightmareharlequin")
public class NightmareHarlequinAI extends GeneralNpcAI {

	public NightmareHarlequinAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		switch (getNpcId()) {
			case 831757, 831758 -> super.handleDialogStart(player);
			default -> PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
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
			SkillEngine.getInstance().getSkill(getOwner(), player.getRace() == Race.ELYOS ? 21470 : 21472, 1, player).useWithoutPropSkill();
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		} else if (dialogActionId == QUEST_SELECT && questId != 0) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10, questId));
		}
		return true;
	}

}
