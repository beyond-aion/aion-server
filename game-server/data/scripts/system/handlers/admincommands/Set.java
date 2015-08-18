package admincommands;

import java.util.Arrays;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TITLE_INFO;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Nemiroff, ATracer, IceReaper Date: 11.12.2009
 * @author Sarynth - Added AP
 * @author Artur - Added GP
 */
public class Set extends AdminCommand {

	public Set() {
		super("set");
	}

	@Override
	public void execute(Player admin, String... params) {
		Player target = null;
		VisibleObject creature = admin.getTarget();

		if (admin.getTarget() instanceof Player) {
			target = (Player) creature;
		}

		if (target == null) {
			PacketSendUtility.sendMessage(admin, "You should select a target first!");
			return;
		}

		if (params.length < 2) {
			PacketSendUtility.sendMessage(admin, "You should enter second params!");
			return;
		}
		String paramValue = params[1];

		if (params[0].equals("class")) {
			byte newClass;
			try {
				newClass = Byte.parseByte(paramValue);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			PlayerClass oldClass = target.getPlayerClass();
			setClass(target, oldClass, newClass);
		}
		else if (params[0].equals("exp")) {
			long exp;
			try {
				exp = Long.parseLong(paramValue);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			target.getCommonData().setExp(exp);
			PacketSendUtility.sendMessage(admin, "Set exp of target to " + paramValue);
		}
		else if (params[0].equals("ap")) {
			int ap;
			try {
				ap = Integer.parseInt(paramValue);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			AbyssPointsService.setAp(target, ap);
			if (target == admin) {
				PacketSendUtility.sendMessage(admin, "Set your Abyss Points to " + ap + ".");
			}
			else {
				PacketSendUtility.sendMessage(admin, "Set " + target.getName() + " Abyss Points to " + ap + ".");
				PacketSendUtility.sendMessage(target, "Admin set your Abyss Points to " + ap + ".");
			}
		}
		else if (params[0].equals("gp")) {
			int gp;
			try {
				gp = Integer.parseInt(paramValue);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			GloryPointsService.addGp(target, gp, false);
			if (target == admin) {
				PacketSendUtility.sendMessage(admin, "Set your Glory Points to " + gp + ".");
			}
			else {
				PacketSendUtility.sendMessage(admin, "Set " + target.getName() + " Glory Points to " + gp + ".");
				PacketSendUtility.sendMessage(target, "Admin set your Glory Points to " + gp + ".");
			}
		}
		else if (params[0].equals("level")) {
			int level;
			try {
				level = Integer.parseInt(paramValue);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			Player player = target;

			if (level <= GSConfig.PLAYER_MAX_LEVEL) {
				int questId = player.getRace() == Race.ELYOS ? 1007 : 2009;
				QuestState qs = player.getQuestStateList().getQuestState(questId);

				if (!player.getPlayerClass().isStartingClass() && level >= 10) {
					if (qs == null) {
						player.getQuestStateList().addQuest(questId, new QuestState(questId, QuestStatus.COMPLETE, 0, 0, null, 0, null));
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, QuestStatus.COMPLETE.value(), 0, 0));
					}
					else if (qs.getStatus() != QuestStatus.COMPLETE) {
						qs.setStatus(QuestStatus.COMPLETE);
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars()
							.getQuestVars(), qs.getFlags()));
					}
					player.getCommonData().setDaeva(true);
					player.getController().upgradePlayer();
				}
				else if (level < 10 && qs == null) {
					// don't delete ceremony quest
					player.getCommonData().setDaeva(false);
				}
				player.getCommonData().setLevel(level);
			}
			
			PacketSendUtility.sendMessage(admin, "Set " + player.getCommonData().getName() + " level to " + level);
		}
		else if (params[0].equals("title")) {
			int titleId;
			try {
				titleId = Integer.parseInt(paramValue);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			Player player = target;
			if (titleId <= 160)
				setTitle(player, titleId);
			PacketSendUtility.sendMessage(admin, "Set " + player.getCommonData().getName() + " title to " + titleId);

		}
	}

	private void setTitle(Player player, int value) {
		PacketSendUtility.sendPacket(player, new SM_TITLE_INFO(value));
		PacketSendUtility.broadcastPacket(player, (new SM_TITLE_INFO(player, value)));
		player.getCommonData().setTitleId(value);
	}

	private void setClass(Player player, PlayerClass oldClass, byte value) {
		PlayerClass playerClass = PlayerClass.getPlayerClassById(value);
		int level = player.getLevel();
		if (level < 9) {
			PacketSendUtility.sendMessage(player, "You can only switch class after reach level 9");
			return;
		}
		if (Arrays.asList(1, 2, 4, 5, 7, 8, 10, 11, 13, 14, 16).contains(oldClass.ordinal())) {
			PacketSendUtility.sendMessage(player, "You already switched class");
			return;
		}
		int newClassId = playerClass.ordinal();
		switch (oldClass.ordinal()) {
			case 0:
				if (newClassId == 1 || newClassId == 2)
					break;
			case 3:
				if (newClassId == 4 || newClassId == 5)
					break;
			case 6:
				if (newClassId == 7 || newClassId == 8)
					break;
			case 9:
				if (newClassId == 10 || newClassId == 11)
					break;
			case 12:
				if (newClassId == 13 || newClassId == 14)
					break;
			case 15:
				if (newClassId == 16)
					break;
			default:
				PacketSendUtility.sendMessage(player, "Invalid class switch chosen");
				return;
		}
		player.getCommonData().setPlayerClass(playerClass);
		player.getController().upgradePlayer();
		PacketSendUtility.sendMessage(player, "You have successfuly switched class");
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //set <class|exp|ap|level|title>");
	}
}
