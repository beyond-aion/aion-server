package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.MoveToMe;

/**
 * This command gets executed from the Summon Group Member button in the GM Panel (Shift + F1).
 */
public class Partyrecall extends ConsoleCommand {

	public Partyrecall() {
		super("partyrecall", "Recalls all members of your party to your location.");
	}

	@Override
	public void execute(Player admin, String... params) {
		PlayerGroup group = admin.getPlayerGroup();
		if (group == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_PARTY_YOU_ARE_NOT_PARTY_MEMBER());
			return;
		}
		group.getOnlineMembers().forEach(p -> MoveToMe.teleportPlayer(p, admin));
	}
}
