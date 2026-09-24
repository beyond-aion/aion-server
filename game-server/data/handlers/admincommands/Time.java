package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GAME_TIME;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.time.gametime.GameTime;

/**
 * @author Pan, Neon, Sykra
 */
public class Time extends AdminCommand {

	public Time() {
		super("time", "Changes the game time.", """
			<dawn|day|dusk|night> - Sets the specified day time.
			<0-23> - Sets the specified hour.
			<0-23> <0-59> - Sets the specified hour and minute.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		int hour;
		int minute = 0;
		if (params[0].equalsIgnoreCase("night")) {
			hour = 22;
		} else if (params[0].equalsIgnoreCase("dusk")) {
			hour = 18;
		} else if (params[0].equalsIgnoreCase("day")) {
			hour = 9;
		} else if (params[0].equalsIgnoreCase("dawn")) {
			hour = 4;
		} else {
			hour = Integer.parseInt(params[0]);
			if (hour < 0 || hour > 23) {
				sendInfo(admin, "Hour must be between 0 and 23.");
				return;
			}
			if (params.length == 2) {
				minute = Integer.parseInt(params[1]);
				if (minute < 0 || minute > 59) {
					sendInfo(admin, "Minute must be between 0 and 59.");
					return;
				}
			}
		}

		GameTime gameTime = GameTimeService.getInstance().getGameTime();
		int hourOffset = hour - gameTime.getHour(); // hour offset inside the same day
		int minutesToAdd = 60 * hourOffset;
		if (minute == 0) {
			minutesToAdd -= gameTime.getMinute();
		} else {
			int minuteOffset = minute - gameTime.getMinute();
			minutesToAdd += minuteOffset;
		}
		gameTime.addMinutes(minutesToAdd);
		PacketSendUtility.broadcastToWorld(new SM_GAME_TIME());
		sendInfo(admin, "You changed the time to " + gameTime.getHour() + ":" + String.format("%02d", gameTime.getMinute()) + ".");
	}
}
