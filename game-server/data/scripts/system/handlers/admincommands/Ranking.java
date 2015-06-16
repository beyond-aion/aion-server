package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.abyss.AbyssRankUpdateService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 */
public class Ranking extends AdminCommand {

	public Ranking() {
		super("ranking");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			onFail(admin, null);
		}
		else if ("update".equalsIgnoreCase(params[0])) {
			AbyssRankUpdateService.getInstance().performUpdate();
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //ranking update");
	}
}
