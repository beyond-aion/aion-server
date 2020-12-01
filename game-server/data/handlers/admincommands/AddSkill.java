package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Phantom
 */
public class AddSkill extends AdminCommand {

	public AddSkill() {
		super("addskill");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 2) {
			PacketSendUtility.sendMessage(player, "syntax //addskill <skillId> <skillLevel>");
			return;
		}

		VisibleObject target = player.getTarget();

		int skillId = 0;
		int skillLevel = 0;

		try {
			skillId = Integer.parseInt(params[0]);
			skillLevel = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(player, "Parameters need to be an integer.");
			return;
		}

		if (target instanceof Player) {
			Player targetpl = (Player) target;
			targetpl.getSkillList().addSkill(targetpl, skillId, skillLevel);
			PacketSendUtility.sendMessage(player, "You have success add skill");
			PacketSendUtility.sendMessage(targetpl, "You have acquire a new skill");
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //addskill <skillId> <skillLevel>");
	}
}
