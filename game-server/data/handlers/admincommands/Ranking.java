package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.abyss.AbyssRankUpdateService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 */
public class Ranking extends AdminCommand {

	public Ranking() {
		super("ranking", "Abyss rank control.");
		setSyntaxInfo("<update> - Runs the daily Abyss rank update task.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 1 && "update".equalsIgnoreCase(params[0]))
			AbyssRankUpdateService.performUpdate();
		else 
			sendInfo(admin);
	}
}
