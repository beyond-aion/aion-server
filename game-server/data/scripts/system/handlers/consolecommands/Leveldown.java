package consolecommands;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Leveldown extends ConsoleCommand {

	public Leveldown() {
		super("leveldown");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		if (!ChatProcessor.getInstance().isCommandAllowed(admin, "set")) {
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command");
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

		int level;
		try {
			level = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "You should enter valid value!");
			return;
		}

		level = player.getLevel() - level;

		if (level <= GSConfig.PLAYER_MAX_LEVEL) {
			int questId = player.getRace() == Race.ELYOS ? 1007 : 2009;
			QuestState qs = player.getQuestStateList().getQuestState(questId);

			if (!player.getPlayerClass().isStartingClass() && level >= 10) {
				if (qs == null) {
					player.getQuestStateList().addQuest(questId, new QuestState(questId, QuestStatus.COMPLETE, 0, 0, null, 0, null));
					PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, QuestStatus.COMPLETE.value(), 0, 0));
				} else if (qs.getStatus() != QuestStatus.COMPLETE) {
					qs.setStatus(QuestStatus.COMPLETE);
					PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars(), qs.getFlags()));
				}
				player.getCommonData().setDaeva(true);
				player.getController().upgradePlayer();
			} else if (level < 10 && qs == null) {
				// don't delete ceremony quest
				player.getCommonData().setDaeva(false);
			}
			player.getCommonData().setLevel(level);
		}
		PacketSendUtility.sendMessage(admin, "Set " + player.getCommonData().getName() + " level to " + level);
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///leveldown <value>");
	}
}
