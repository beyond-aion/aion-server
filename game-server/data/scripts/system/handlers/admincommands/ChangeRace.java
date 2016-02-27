package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ginho1
 */
public class ChangeRace extends AdminCommand {

	public ChangeRace() {
		super("changerace", "Switches your race to the opposite faction.");
	}

	@Override
	public void execute(Player admin, String... params) {
		admin.getCommonData().setRace(admin.getOppositeRace());
		admin.clearKnownlist();
		PacketSendUtility.sendPacket(admin, new SM_PLAYER_INFO(admin));
		admin.updateKnownlist();
	}
}
