package ai.portals;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Estrayl
 */
@AIName("custom_instance_teleporter")
public class CustomInstanceTeleporter extends ActionItemNpcAI {

	public CustomInstanceTeleporter(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (player.getLevel() < 65) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_LEVEL());
			return;
		}
		if (!CustomInstanceService.getInstance().canEnter(player.getObjectId())) {
			PacketSendUtility.sendMessage(player, "You have already done this instance for today. You can re-enter it after 9 AM.",
				ChatType.BRIGHT_YELLOW_CENTER);
			return;
		}
		CustomInstanceService.getInstance().onEnter(player);
	}
}
