package consolecommands;

import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */

public class Deletecquest extends ConsoleCommand {

	public Deletecquest() {
		super("deletecquest");
	}

	@Override
	public void execute(Player admin, String... params) {
		if ((params.length < 0) || (params.length < 1)) {
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

		int id;
		try {
			id = Integer.valueOf(params[0]);
		} catch (NumberFormatException e) {
			info(admin, null);
			return;
		}

		QuestStateList list = player.getQuestStateList();
		if (list == null || list.getQuestState(id) == null) {
			PacketSendUtility.sendMessage(admin, "Quest not deleted.");
		}

		QuestState qs = list.getQuestState(id);

		if (qs != null) {
			qs.setQuestVar(0);
			qs.setCompleteCount(0);
			qs.setFlags(0);
			qs.setStatus(null);
			if (qs.getPersistentState() != PersistentState.NEW)
				qs.setPersistentState(PersistentState.DELETED);
			QuestEngine.getInstance().sendCompletedQuests(player);
			player.getController().updateNearbyQuests();
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax ///deletecquest 3 <id quest>");
	}

}
