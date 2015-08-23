package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.MonsterRaidService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;


/**
 * @author Whoop
 *
 */
public class MonsterRaid extends AdminCommand {
	
	private static final String START = "start";
	private static final String STOP = "stop";

	public MonsterRaid() {
		super("monsterraid");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length != 2 || !NumberUtils.isDigits(params[1])) {
			onFail(admin, "SYNTAX: //monsterraid start|stop <LocationId>");
			return;
		}
		
		int raidLocId = NumberUtils.toInt(params[1]);
		if (!isValidRaidLocation(admin, raidLocId))
			return;

		if (START.equalsIgnoreCase(params[0])) {
			if (MonsterRaidService.getInstance().isRaidInProgress(raidLocId)) {
				PacketSendUtility.sendMessage(admin, "Raid Location " + raidLocId + " is already active");
			}
			else {
				PacketSendUtility.sendMessage(admin, "Raid Location " + raidLocId + " - starting monster raid!");
				MonsterRaidService.getInstance().startRaid(raidLocId);
			}			
		} else if (STOP.equalsIgnoreCase(params[0])) {
			if (!MonsterRaidService.getInstance().isRaidInProgress(raidLocId)) {
				PacketSendUtility.sendMessage(admin, "Raid Location " + raidLocId + " is not active");
			}
			else {
				PacketSendUtility.sendMessage(admin, "Raid Location " + raidLocId + " - stopping monster raid!");
				MonsterRaidService.getInstance().stopRaid(raidLocId);
			}
		}
	}
	
	private boolean isValidRaidLocation(Player player, int locId) {
		
		if (!MonsterRaidService.getInstance().getMonsterRaidLocations().keySet().contains(locId)){
			PacketSendUtility.sendMessage(player, "Id " + locId + " is invalid");
			return false;
		}			
		return true;
	}
	
	@Override
	public void onFail(Player player, String msg) {
		PacketSendUtility.sendMessage(player, msg);
	}
}
