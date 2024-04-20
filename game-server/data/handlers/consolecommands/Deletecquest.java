package consolecommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Deletecquest extends ConsoleCommand {

	public Deletecquest() {
		super("deletecquest", "Deletes a quest from the players quest list.");

		setSyntaxInfo("<3> <quest> - Deletes the quest from the targets quest list.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		VisibleObject target = admin.getTarget();
		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "Please select a player.");
			return;
		}

		Player player = (Player) target;
		int questId;
		try {
			questId = Integer.valueOf(params[0]);
		} catch (NumberFormatException e) {
			sendInfo(admin);
			return;
		}

		QuestState qs = player.getQuestStateList().deleteQuest(questId);
		if (qs == null) {
			sendInfo(admin, "Player " + player.getName() + " does not have that quest.");
			return;
		}
		if (qs.getStatus() == QuestStatus.COMPLETE)
			QuestEngine.getInstance().sendCompletedQuests(player); // rewrite completed quest list
		else
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ABANDON, qs));
		player.getController().updateNearbyQuests();
	}
}
