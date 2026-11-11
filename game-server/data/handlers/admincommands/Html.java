package admincommands;

import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.HTMLService;
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
			sendInfo(player, HTMLCache.getInstance().toString());
		} else if (params[0].equals("show") && params.length >= 2) {
			String html = HTMLCache.getInstance().getHTML(params[1] + ".xhtml");
			if (html == null)
				sendInfo(player, "There is no HTML page " + params[1] + ".xhtml.");
			else
				HTMLService.showHTML(player, html);
		} else {
			sendInfo(player);
		}
	}
}
