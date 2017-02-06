package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.player.PlayerMailboxState;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;

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
			AionObject object = World.getInstance().findVisibleObject(targetObjectId);
			if (object instanceof Npc) {
				Npc znpc = (Npc) object;
				if (znpc.getNpcId() == 798100 || znpc.getNpcId() == 798101) {
					player.getMailbox().mailBoxState = PlayerMailboxState.EXPRESS;
					writeH(2);
				} else
					player.getMailbox().mailBoxState = PlayerMailboxState.REGULAR;
			} else
				writeH(0);
		} else if (dialogPageId == DialogPage.TOWN_CHALLENGE_TASK.id()) {
			AionObject object = World.getInstance().findVisibleObject(targetObjectId);
			if (object instanceof Npc) {
				Npc npc = (Npc) object;
				if (npc.getNpcId() == 205770 || npc.getNpcId() == 730677 || npc.getNpcId() == 730679) {
					int townId = 0;
					for (ZoneInstance zone : npc.findZones()) {
						townId = zone.getTownId();
						if (townId > 0)
							break;
					}
					writeH(townId);
				}
			}
		} else
			writeH(0);
	}

}
