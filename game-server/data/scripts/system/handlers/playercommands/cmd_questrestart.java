package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author ginho1
 *
 */
public class cmd_questrestart extends PlayerCommand {

	public cmd_questrestart() {
		super("questrestart");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(player, "syntax .questrestart <quest id>");
			return;
		}

		int id;
		try {
			id = Integer.valueOf(params[0]);
		}
		catch (NumberFormatException e)	{
			PacketSendUtility.sendMessage(player, "syntax .questrestart <quest id>");
			return;
		}

		QuestState qs = player.getQuestStateList().getQuestState(id);

		if (qs == null || id == 1006 || id == 2008 || id == 10021 || id == 20021 || id == 18602 || id == 28602) {
			PacketSendUtility.sendMessage(player, "Quest [quest: "+id+"] can't be restarted.");
			return;
		}

		if (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD) {
			if(qs.getQuestVarById(0) != 0) {
				qs.setStatus(QuestStatus.START);
				qs.setQuestVar(0);
				PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(id, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
				PacketSendUtility.sendMessage(player, "Quest [quest: "+id+"] restarted.");
			} else
				PacketSendUtility.sendMessage(player, "Quest [quest: "+id+"] can't be restarted.");
		} else	{
			PacketSendUtility.sendMessage(player, "Quest [quest: "+id+"] can't be restarted.");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
