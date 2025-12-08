package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.TownService;

/**
 * @author alexa026
 */
public class SM_DIALOG_WINDOW extends AionServerPacket {

	private final int targetObjectId;
	private final int dialogPageId;
	private final int questId;

	public SM_DIALOG_WINDOW(int targetObjectId, int dialogPageId) {
		this(targetObjectId, dialogPageId, 0);
	}

	public SM_DIALOG_WINDOW(int targetObjectId, int dialogPageId, int questId) {
		this.targetObjectId = targetObjectId;
		this.dialogPageId = dialogPageId;
		this.questId = questId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		writeD(targetObjectId);
		writeH(dialogPageId);
		writeD(questId);
		writeH(0);
		if (dialogPageId == DialogPage.MAIL.id()) {
			writeH(player.getMailbox().mailBoxState);
		} else if (dialogPageId == DialogPage.TOWN_CHALLENGE_TASK.id()) {
			writeH(TownService.getInstance().getTownIdByPosition(player));
		} else
			writeH(0);
	}

}
