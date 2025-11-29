package playercommands;

import java.awt.Color;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.reward.AdventService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Advent extends PlayerCommand {

	public Advent() {
		super("advent", "Gets your advent reward for today.");

		// @formatter:off
		setSyntaxInfo(
			"show - Shows today's reward.",
			"get - Gets your reward for today on this character.\n" + ChatUtil.color("ATTENTION:", Color.PINK) + " Only one character per account can receive this reward!"
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 1)
			sendInfo(player);
		else if ("show".equalsIgnoreCase(params[0]))
			AdventService.getInstance().showTodaysReward(player);
		else if ("get".equalsIgnoreCase(params[0]))
			AdventService.getInstance().redeemReward(player);
	}

	@Override
	public boolean validateAccess(Player player) {
		if (!super.validateAccess(player))
			return false;
		if (!EventsConfig.ENABLE_ADVENT_CALENDAR) {
			if (player.isStaff())
				sendInfo(player, "The advent calendar is currently disabled.");
			return false;
		}
		if (!AdventService.getInstance().isAdventSeason()) {
			if (player.isStaff())
				sendInfo(player, "This command is only active during the Advent season.");
			return false;
		}
		return true;
	}
}
