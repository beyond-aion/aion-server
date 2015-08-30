package consolecommands;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Classup extends ConsoleCommand {

	public Classup() {
		super("classup");
	}

	@Override
	public void execute(Player admin, String... params) {
		if ((params.length < 0) || (params.length < 1)) {
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

		if (player.getLevel() < 10) {
			// why is this called and then not used in any meaningful way?
//			int questId = player.getRace() == Race.ELYOS ? 1007 : 2009;
//			QuestState qs = player.getQuestStateList().getQuestState(questId);
			int level = 10;
			player.getCommonData().setDaeva(true);
			player.getCommonData().setLevel(level);
			player.getController().upgradePlayer();
		}
		
		String newClass = params[0];

		if(newClass.equals("fighter"))
			newClass = "GLADIATOR";
		if(newClass.equals("knight"))
			newClass = "TEMPLAR";
		if(newClass.equals("wizard"))
			newClass = "SORCERER";
		if(newClass.equals("elementalist"))
			newClass = "SPIRIT_MASTER";

		setClass(player, newClass);
	}

	private void setClass(Player player, String value) {
		PlayerClass playerClass = PlayerClass.getPlayerClassByString(value.toUpperCase());
		player.getCommonData().setPlayerClass(playerClass);
		player.getController().upgradePlayer();
		PacketSendUtility.sendMessage(player, "You have successfuly switched class");
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///classup <value>");
	}
}