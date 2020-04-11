package playercommands;

import java.awt.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;


public class Nomorph extends PlayerCommand {

	public Nomorph() {
		super("nomorph", "Enables/disables your current transformation appearance.");
	}

	@Override
	protected void execute(Player player, String... params) {
		if (player.getTransformModel().getEventModelId() == player.getObjectTemplate().getTemplateId()) {
			player.getTransformModel().setEventModelId(0);
		} else {
			player.getTransformModel().setEventModelId(player.getObjectTemplate().getTemplateId());
		}
		player.getTransformModel().updateVisually();
		sendInfo(player, "Transformation appearance is now " + (player.getTransformModel().getEventModelId() == player.getObjectTemplate().getTemplateId() ? ChatUtil.color("inactive", Color.RED) : ChatUtil.color("active", Color.GREEN)) + ".");
	}
}
