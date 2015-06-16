package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_PACKET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_PACKET.PacketElementType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * This server command is used for creating and sending custom packets from server to client. It's used in development
 * purpose.<br>
 * <b>command name: //fsc</b></br> <b>params:</b>
 * <ul>
 * <li>packet id (it's one byte) - maybe in dec format (for example 227), but may be also in hex format (for example
 * 0xE3)</li>
 * <li>package format string - string containing with letters: d (represents writeD()), h (represents writeH()), c
 * (represents writeC()), f (represents writeF()), e (represents write DF()), q (represents writeQ()), s (represents
 * writeS())</li>
 * <li>list of data - here goes all data for corresponding to proper format parts.</li>
 * </ul>
 * Example:<br>
 * //fsc 0xD8 cdds 8 50 80 someText - will send packet with id 0xD8 (subids will be added automaticaly) then will be
 * sent one byte - 8, later two ints -50 and 80 and at the end a String - someText
 * 
 * @author Luno
 */
public class Fsc extends AdminCommand {

	public Fsc() {
		super("fsc");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 3) {
			PacketSendUtility.sendMessage(player, "Incorrent number of params in //fsc command");
			return;
		}

		int id = Integer.decode(params[0]);
		String format = "";

		if (params.length > 1)
			format = params[1];

		SM_CUSTOM_PACKET packet = new SM_CUSTOM_PACKET(id);

		int i = 0;
		for (char c : format.toCharArray()) {
			packet.addElement(PacketElementType.getByCode(c), params[i + 2]);
			i++;
		}
		PacketSendUtility.sendPacket(player, packet);
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Incorrent number of params in //fsc command");
	}
}
