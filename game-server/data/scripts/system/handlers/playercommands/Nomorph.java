package playercommands;

import com.aionemu.gameserver.model.gameobjects.TransformModel;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.TransformEffect;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Nomorph extends PlayerCommand {

	private static List<Integer> demorphedPlayers = new ArrayList<>();

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
