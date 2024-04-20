package consolecommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Endquest extends ConsoleCommand {

	public Endquest() {
		super("endquest", "Completes a quest.");

		setSyntaxInfo("<quest> - Completes the specified quest (without giving rewards).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		VisibleObject target = admin.getTarget();
		if (!(target instanceof Player)) {
			sendInfo(admin, "Please select a player.");
			return;
		}

		Player player = (Player) target;
		int questId = ChatUtil.getQuestId(params[0]);
		if (questId == 0) {
			sendInfo(admin, "Invalid quest link or ID.");
			return;
		}

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			sendInfo(admin, "Quest must be started first.");
			return;
		}
		qs.setStatus(QuestStatus.COMPLETE);
		qs.setQuestVar(0);
		if (!DataManager.QUEST_DATA.getQuestById(qs.getQuestId()).getRewards().isEmpty())
			qs.setRewardGroup(0); // follow quests could require reward group > 0 to be unlocked (see quest_data.xml)
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
		QuestEngine.getInstance().onQuestCompleted(player, qs.getQuestId());
		player.getController().updateNearbyQuests();
	}
}
