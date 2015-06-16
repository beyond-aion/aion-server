package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Elusive
 */
public class Kick extends AdminCommand {

	public Kick() {
		super("kick");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			PacketSendUtility.sendMessage(admin, "syntax //kick <character_name> | <All>");
			return;
		}
		
		if(params[0] != null && "All".equalsIgnoreCase(params[0])){
			for (final Player player : World.getInstance().getAllPlayers()) {
				if(!player.isGM()){
					player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
					PacketSendUtility.sendMessage(admin, "Kicked player : " + player.getName());
				}
			}
		}else{
			Player player = World.getInstance().findPlayer(Util.convertName(params[0]));
			if (player == null) {
				PacketSendUtility.sendMessage(admin, "The specified player is not online.");
				return;
			}
			player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
			PacketSendUtility.sendMessage(admin, "Kicked player : " + player.getName());
		}
		
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //kick <character_name> | <All>");
	}
}
