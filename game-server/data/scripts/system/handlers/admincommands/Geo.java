package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.geo.GeoService;


/**
 * @author MrPoke
 *
 */
public class Geo extends AdminCommand{

	public Geo() {
		super("geo");
	}

	@Override
	public void execute(Player player, String... params) {
		if ("z".startsWith(params[0])){
			PacketSendUtility.sendMessage(player, "GeoZ: "+GeoService.getInstance().getZ(player)+ " current Z: "+player.getZ());
		}
	}
	
}
