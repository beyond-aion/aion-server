package admincommands;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Divinity, Neon
 */
public class Say extends AdminCommand {

	public Say() {
		super("say", "Let's your target say a message.");

		setSyntaxInfo("<message> - Sends the message as your target (npc only).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if (!(admin.getTarget() instanceof Npc npc)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		PacketSendUtility.broadcastPacket(admin, new SM_MESSAGE(npc, String.join(" ", params), ChatType.NORMAL), true);
	}
}
