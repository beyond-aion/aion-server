package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dao.BookmarkDAO.Bookmark;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Yeats
 */
public class SM_GM_BOOKMARK_ADD extends AionServerPacket {

	private final Bookmark bookmark;

	public SM_GM_BOOKMARK_ADD(Bookmark bookmark) {
		this.bookmark = bookmark;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(bookmark.name());
		writeD(bookmark.worldId());
		writeF(bookmark.x());
		writeF(bookmark.y());
		writeF(bookmark.z());
	}
}
