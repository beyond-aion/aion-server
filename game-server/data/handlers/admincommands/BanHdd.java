package admincommands;

import java.sql.Timestamp;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ban.HDDBanService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ViAl
 */
public class BanHdd extends AdminCommand {

	private static String SYNTAX = "Syntax: //banhdd <hdd_serial> <time_in_minutes|0 - infinite>";

	public BanHdd() {
		super("banhdd");
	}

	@Override
	public void execute(Player player, String... params) {
		try {
			String hddSerial = params[0];
			Integer timeMins = Integer.parseInt(params[1]);
			if (timeMins == 0)
				timeMins = 10 * 365 * 24 * 60;
			Timestamp banTime = new Timestamp(System.currentTimeMillis() + timeMins * 60 * 1000);
			HDDBanService.getInstance().addBan(hddSerial, banTime);
		} catch (Exception e) {
			PacketSendUtility.sendMessage(player, SYNTAX);
		}
	}

}
