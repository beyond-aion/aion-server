package admincommands;

import com.aionemu.gameserver.dao.BookmarkDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

import consolecommands.Bookmark_add;

public class Bookmark extends AdminCommand {

	public Bookmark() {
		super("bookmark", "Manages teleport bookmarks.");

		// @formatter:off
		setSyntaxInfo(
			"del <name> - Deletes the bookmark with the specified name.",
			"deleteAll - Deletes all bookmarks.",
			"Note: Press Shift+G and click the \"Bookmark\" button to add or use your teleport bookmarks."
		);
		// @formatter:on
	}

	@Override
	protected String getAliasForLevel() {
		return Bookmark_add.ALIAS;
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length >= 2 && params[0].equalsIgnoreCase("del")) {
			String bookmarkName = String.join(" ", params).substring(4);
			boolean deleted = BookmarkDAO.deleteBookmark(player.getObjectId(), bookmarkName);
			if (deleted) {
				sendInfo(player, "The bookmark has been deleted. The bookmarks list will be updated after relog.");
			} else {
				sendInfo(player, "No bookmark with that name was found.");
			}
		} else if (params.length >= 1 && params[0].equalsIgnoreCase("deleteAll")) {
			BookmarkDAO.deleteAll(player.getObjectId());
			sendInfo(player, "All bookmarks have been deleted. The bookmarks list will be updated after relog.");
		} else {
			sendInfo(player);
		}
	}
}
