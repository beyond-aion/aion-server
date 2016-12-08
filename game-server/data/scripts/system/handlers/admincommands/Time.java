package admincommands;

import java.security.InvalidParameterException;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GAME_TIME;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.time.gametime.GameTime;
import com.aionemu.gameserver.world.World;

/**
 * @author Pan
 */
public class Time extends AdminCommand {

	public Time() {
		super("time", "Changes the game time.");

		setParamInfo(
			"<dawn|day|dusk|night> - Sets the specified day time.",
			"<0-23> - Sets the specified hour."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		int hour;
		if (params[0].equalsIgnoreCase("night")) {
			hour = 22;
		} else if (params[0].equalsIgnoreCase("dusk")) {
			hour = 18;
		} else if (params[0].equalsIgnoreCase("day")) {
			hour = 9;
		} else if (params[0].equalsIgnoreCase("dawn")) {
			hour = 4;
		} else {
			try {
				hour = Integer.parseInt(params[0]);
				if (hour < 0 || hour > 23)
					throw new InvalidParameterException("A day has only 24 hours!\nMin value: 0 - Max value: 23");
			} catch (NumberFormatException | InvalidParameterException e) {
				sendInfo(admin, e instanceof InvalidParameterException ? e.getMessage() : null);
				return;
			}
		}

		GameTime gameTime = GameTimeService.getInstance().getGameTime();
		int hourOffset = hour - GameTimeService.getInstance().getGameTime().getHour(); // hour offset inside the same day
		gameTime.addMinutes((60 * hourOffset) - gameTime.getMinute());

		World.getInstance().forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_GAME_TIME()));

		sendInfo(admin, "You changed the time to " + gameTime.getHour() + ":" + String.format("%02d", gameTime.getMinute()) + ".");
	}
}
