package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_SHOW_PLAYER_STATUS;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 */
public class Status extends ConsoleCommand {

	public Status() {
		super("status");
	}

	@Override
	protected void execute(Player admin, String... params) {
		Player target = null;
		if (params.length > 0) {
			target = World.getInstance().getPlayer(params[0]);
		}
		if (target == null && admin.getTarget() instanceof Player) {
			target = (Player) admin.getTarget();
		}
		if (target != null) {
			PacketSendUtility.sendPacket(admin, new SM_GM_SHOW_PLAYER_STATUS(target));
		}
	}
}
