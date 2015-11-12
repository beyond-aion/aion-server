package consolecommands;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */

public class Endquest extends ConsoleCommand {

	public Endquest() {
		super("endquest");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		int questId;
		QuestStatus questStatus = QuestStatus.COMPLETE;
		try {
			String quest = params[0];
			Pattern id = Pattern.compile("\\[quest:([^%]+)]");
			Matcher result = id.matcher(quest);
			if (result.find())
				questId = Integer.parseInt(result.group(1));
			else
				questId = Integer.parseInt(params[0]);

		} catch (NumberFormatException e) {
			info(admin, null);
			return;
		}

		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null) {
			qs = new QuestState(questId, questStatus, 0, 0, new Timestamp(0), 0, new Timestamp(0));
			player.getQuestStateList().addQuest(questId, qs);
			PacketSendUtility.sendMessage(admin, "<QuestState has been newly initialized.>");
			return;
		}

		qs.setStatus(questStatus);

		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));

		qs.setCompleteCount(qs.getCompleteCount() + 1);
		player.getController().updateNearbyQuests();

	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax ///endquest <id quest>");
	}

}
