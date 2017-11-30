package playercommands;

import java.awt.Color;

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

		setSyntaxInfo("<show> - Shows todays reward.", "<get> - Gets your reward for today on this character.\n" + ChatUtil.color("ATTENTION!", Color.RED)
			+ " Only one character per account can receive this reward!");
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
}
