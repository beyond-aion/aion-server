package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.MonsterRaidService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Whoop
 */
public class MonsterRaid extends AdminCommand {

	private static final String START = "start";
	private static final String STOP = "stop";

	public MonsterRaid() {
		super("monsterraid", "Starts or stops the Beritra Invasion event");
		
		setParamInfo("<start|stop> <location id> - starts or stops the Beritra Invasion event for a specific location");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 2 || !NumberUtils.isDigits(params[1])) {
			sendInfo(player);
			return;
		}

		int raidLocId = NumberUtils.toInt(params[1]);
		if (!isValidRaidLocation(raidLocId)) {
			sendInfo(player, "ID " + raidLocId + " is invalid");
			return;
		}

		if (START.equalsIgnoreCase(params[0])) {
			if (MonsterRaidService.getInstance().isRaidInProgress(raidLocId)) {
				sendInfo(player, "Raid Location " + raidLocId + " is already active");
			} else {
				sendInfo(player, "Raid Location " + raidLocId + " - starting monster raid!");
				MonsterRaidService.getInstance().startRaid(raidLocId);
			}
		} else if (STOP.equalsIgnoreCase(params[0])) {
			if (!MonsterRaidService.getInstance().isRaidInProgress(raidLocId)) {
				sendInfo(player, "Raid Location " + raidLocId + " is not active");
			} else {
				sendInfo(player, "Raid Location " + raidLocId + " - stopping monster raid!");
				MonsterRaidService.getInstance().stopRaid(raidLocId);
			}
		}
	}

	private boolean isValidRaidLocation(int locId) {
		return MonsterRaidService.getInstance().getMonsterRaidLocations().keySet().contains(locId);
	}
}
