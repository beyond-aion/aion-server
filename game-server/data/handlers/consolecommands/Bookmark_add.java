package consolecommands;

import com.aionemu.gameserver.dao.BookmarkDAO;
import com.aionemu.gameserver.dao.BookmarkDAO.Bookmark;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_BOOKMARK_ADD;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Bookmark_add extends ConsoleCommand {

	public static final String ALIAS = "Bookmark_add";

	public Bookmark_add() {
		super(ALIAS);
	}

	@Override
	public void execute(Player admin, String... params) {
		String bookmarkName = String.join(" ", params);
		if (bookmarkName.isEmpty())
			return;
		if (bookmarkName.length() > 27)
			bookmarkName = bookmarkName.substring(0, 27);
		Bookmark bookmark = new Bookmark(bookmarkName, admin.getWorldId(), admin.getX(), admin.getY(), admin.getZ());
		BookmarkDAO.storeBookmark(admin.getObjectId(), bookmark);
		PacketSendUtility.sendPacket(admin, new SM_GM_BOOKMARK_ADD(bookmark));
	}
}
