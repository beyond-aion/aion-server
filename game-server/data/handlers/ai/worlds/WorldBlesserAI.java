package ai.worlds;

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
@AIName("world_blesser")
public class WorldBlesserAI extends GeneralNpcAI {

	public WorldBlesserAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		switch (getNpcId()) {
			case 831030: // Netalion
			case 831024: // Renniah
			case 831025: // Erdat
			case 831026: // Erdat
			case 831028: // Erdat
			case 831027: // Karzanke
				super.handleDialogStart(player);
				break;
			default: {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
				break;
			}
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
			// int chance = Rnd.get(1, 2);
			// 951: Blessing of Health I, 955: Blessing of Rock I : 3.9
			// 20950 : Blessing of Growth : 4.0
			SkillEngine.getInstance().getSkill(getOwner(), 20950, 1, player).useWithoutPropSkill();
		} else if (dialogActionId == QUEST_SELECT && questId != 0) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10, questId));
		}
		return true;
	}

}
