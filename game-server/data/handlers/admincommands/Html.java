package admincommands;

import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author lord_rex
 */
public class Html extends AdminCommand {

	public Html() {
		super("html", "Manages HTML pages.", """
			reload - Reloads the HTML cache.
			show <filename> - Shows the HTML page (file name without .xhtml).
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		if (params[0].equals("reload")) {
			HTMLCache.getInstance().reload(true);
			PacketSendUtility.sendMessage(player, HTMLCache.getInstance().toString());
		} else if (params[0].equals("show"))
			if (params.length >= 2)
				HTMLService.showHTML(player, HTMLCache.getInstance().getHTML(params[1] + ".xhtml"));
			else
				sendInfo(player);
	}
}
