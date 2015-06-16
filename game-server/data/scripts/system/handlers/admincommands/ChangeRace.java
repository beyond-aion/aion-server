package admincommands;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
* @author ginho1
*
*/
public class ChangeRace extends AdminCommand {
	public ChangeRace() {
		super("changerace");
	}

	@Override
	public void execute(Player admin, String... params) {

		if(admin.getCommonData().getRace() == Race.ELYOS)
			admin.getCommonData().setRace(Race.ASMODIANS);
		else
			admin.getCommonData().setRace(Race.ELYOS);

		admin.clearKnownlist();
		PacketSendUtility.sendPacket(admin, new SM_PLAYER_INFO(admin, false));
		admin.updateKnownlist();
	}
}
