package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.world.World;

import admincommands.MoveToMe;

/**
 * This command gets executed from the Summon button in the GM Dialog (Shift + G).
 */
public class Recall extends ConsoleCommand {

	public Recall() {
		super("recall", "Recalls a player to your location.", """
			<name> - Teleports the given player to you.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		String playerName = ChatUtil.getRealCharName(params[0], true);
		Player playerToMove = World.getInstance().getPlayer(playerName);
		if (playerToMove == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			return;
		}
		MoveToMe.teleportPlayer(playerToMove, admin);
	}
}
