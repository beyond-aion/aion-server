package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.MonsterRaidService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Whoop
 */
public class MonsterRaid extends AdminCommand {

	public MonsterRaid() {
		super("monsterraid", "Starts/stops the Beritra Invasion event.");

		setParamInfo("<start|stop> <location id> - Starts or stops the Beritra Invasion event for a specific location.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 2 || !NumberUtils.isDigits(params[1])) {
			sendInfo(player);
			return;
		}

		int raidLocId = NumberUtils.toInt(params[1]);
		if (!MonsterRaidService.getInstance().getMonsterRaidLocations().keySet().contains(raidLocId)) {
			sendInfo(player, "ID " + raidLocId + " is invalid");
			return;
		}

		if (params[0].equalsIgnoreCase("start")) {
			if (MonsterRaidService.getInstance().isRaidInProgress(raidLocId)) {
				sendInfo(player, "Raid Location " + raidLocId + " is already active");
			} else {
				sendInfo(player, "Raid Location " + raidLocId + " - started.");
				MonsterRaidService.getInstance().startRaid(raidLocId);
			}
		} else if (params[0].equalsIgnoreCase("stop")) {
			if (!MonsterRaidService.getInstance().isRaidInProgress(raidLocId)) {
				sendInfo(player, "Raid Location " + raidLocId + " is not active.");
			} else {
				sendInfo(player, "Raid Location " + raidLocId + " - stopped.");
				MonsterRaidService.getInstance().stopRaid(raidLocId);
			}
		}
	}
}
