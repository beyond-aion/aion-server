package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author ginho1
 * @modified Neon
 */
public class Questrestart extends PlayerCommand {

	public Questrestart() {
		super("questrestart", "Restarts a bugged Quest.");

		setParamInfo("<quest link|ID> - restarts the specified quest.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length == 0) {
			sendInfo(player);
			return;
		}

		int id = ChatUtil.getQuestId(params[0]);
		if (id == 0) {
			sendInfo(player, "Invalid quest.");
			return;
		}

		QuestState qs = player.getQuestStateList().getQuestState(id);
		if (qs == null || (qs.getStatus() != QuestStatus.START && qs.getStatus() != QuestStatus.REWARD)) {
			sendInfo(player, "Only currently active quests can be restarted.");
			return;
		}

		if (id == 1006 || id == 2008 || id == 10021 || id == 20021 || id == 18602 || id == 28602) {
			sendInfo(player, "Quest " + ChatUtil.quest(id) + " can't be restarted.");
			return;
		}

		if (qs.getQuestVarById(0) == 0) {
			sendInfo(player, "Restarting quest " + ChatUtil.quest(id) + " would have no effect.");
			return;
		}

		qs.setStatus(QuestStatus.START);
		qs.setQuestVar(0);
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(id, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
		sendInfo(player, "Quest " + ChatUtil.quest(id) + " was restarted.");
	}
}
