package admincommands;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ban.HDDBanService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ViAl
 */
public class BanHdd extends AdminCommand {

	public BanHdd() {
		super("banhdd", "Bans a HDD serial.", """
			<serial> - Bans the HDD serial permanently.
			<serial> <days> - Bans the HDD serial for the given amount of days.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1) {
			sendInfo(player);
			return;
		}
		String hddSerial = params[0];
		Timestamp banTime;
		String message = "Banned HDD serial \"" + hddSerial + "\" ";
		if (params.length > 1) {
			int timeInDays = Math.clamp(Integer.parseInt(params[1]), 1, 365 * 100);
			banTime = new Timestamp(System.currentTimeMillis() + Duration.ofDays(timeInDays).toMillis());
			message += "for " + (timeInDays == 1 ? 1 + "day." : timeInDays + " days.");
		} else {
			banTime = Timestamp.valueOf(LocalDateTime.of(2038, 1, 1, 0, 0, 0));
			message += "until 2038.";
		}
		HDDBanService.getInstance().addBan(hddSerial, banTime);
		sendInfo(player, message);
	}

}
