package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.title.Title;
import com.aionemu.gameserver.model.gameobjects.player.title.TitleList;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author cura, xTz, -Enomine-
 */
public class SM_TITLE_INFO extends AionServerPacket {

	private TitleList titleList;
	private int action; // 0: list, 1: self set, 3: broad set
	private int titleId;
	private int bonusTitleId;
	private int playerObjId;

	public SM_TITLE_INFO(Player player) {
		this.action = 0;
		this.titleList = player.getTitleList();
	}

	public SM_TITLE_INFO(int titleId) {
		this.action = 1;
		this.titleId = titleId;
	}

	public SM_TITLE_INFO(Player player, int titleId) {
		this.action = 3;
		this.playerObjId = player.getObjectId();
		this.titleId = titleId;
	}

	public SM_TITLE_INFO(boolean flag) {
		this.action = 4;
		this.titleId = flag ? 1 : 0;
	}

	public SM_TITLE_INFO(Player player, boolean flag) {
		this.action = 5;
		this.playerObjId = player.getObjectId();
		this.titleId = flag ? 1 : 0;
	}

	public SM_TITLE_INFO(int action, int bonusTitleId) {
		this.action = action;
		this.bonusTitleId = bonusTitleId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		switch (action) {
			case 0:
				writeC(0x00);
				writeH(titleList.size());
				for (Title title : titleList.getTitles()) {
					writeD(title.getId());
					writeD(title.secondsUntilExpiration());
				}
				break;
			case 1: // self set
				writeH(titleId);
				break;
			case 3: // broad set
				writeD(playerObjId);
				writeH(titleId);
				break;
			case 4: // Mentor flag self
				writeH(titleId);
				break;
			case 5: // broad set mentor fleg
				writeD(playerObjId);
				writeH(titleId);
				break;
			case 6:// Title wich will take BonusStats from
				writeH(bonusTitleId);
		}
	}
}
