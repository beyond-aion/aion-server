package admincommands;

import java.lang.reflect.Field;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author SheppeR
 */
public class Channel extends AdminCommand {

	public Channel() {
		super("channel");
	}
	
	@Override
	public void execute(Player player, String... params) {
		Class<?> classToMofify = CustomConfig.class;
		Field someField;
		try {
			someField = classToMofify.getDeclaredField("FACTION_CMD_CHANNEL");
			if (params[0].equalsIgnoreCase("on") && !CustomConfig.FACTION_CMD_CHANNEL) {
				someField.set(null, Boolean.valueOf(true));
				PacketSendUtility.sendMessage(player, "The command .faction is ON.");
			}
			else if (params[0].equalsIgnoreCase("off") && CustomConfig.FACTION_CMD_CHANNEL) {
				someField.set(null, Boolean.valueOf(false));
				PacketSendUtility.sendMessage(player, "The command .faction is OFF.");
			}
		}
		catch (Exception e) {
			PacketSendUtility.sendMessage(player, "Error! Wrong property or value.");
			return;
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //channel <On | Off>");
	}
}
