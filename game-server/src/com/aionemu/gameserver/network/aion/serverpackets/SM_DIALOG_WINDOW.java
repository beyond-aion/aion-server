package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.player.PlayerMailboxState;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author alexa026
 */
public class SM_DIALOG_WINDOW extends AionServerPacket {

	private int targetObjectId;
	private int dialogID;
	private int questId = 0;

	public SM_DIALOG_WINDOW(int targetObjectId, int dlgID) {
		this.targetObjectId = targetObjectId;
		this.dialogID = dlgID;
	}

	public SM_DIALOG_WINDOW(int targetObjectId, int dlgID, int questId) {
		this.targetObjectId = targetObjectId;
		this.dialogID = dlgID;
		this.questId = questId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();

		writeD(targetObjectId);
		writeH(dialogID);
		writeD(questId);
		writeH(0);
		if (this.dialogID == DialogPage.MAIL.id()) {
			AionObject object = World.getInstance().findVisibleObject(targetObjectId);
			if (object != null && object instanceof Npc) {
				Npc znpc = (Npc) object;
				if (znpc.getNpcId() == 798100 || znpc.getNpcId() == 798101) {
					player.getMailbox().mailBoxState = PlayerMailboxState.EXPRESS;
					writeH(2);
				}
				else
					player.getMailbox().mailBoxState = PlayerMailboxState.REGULAR;
			}
			else
				writeH(0);
		}
		else if (this.dialogID == DialogPage.TOWN_CHALLENGE_TASK.id()) {
			AionObject object = World.getInstance().findVisibleObject(targetObjectId);
			if (object != null && object instanceof Npc) {
				Npc npc = (Npc) object;
				if (npc.getNpcId() == 205770 || npc.getNpcId() == 730677 || npc.getNpcId() == 730679) {
					int townId = 0;
					MapRegion region = npc.getPosition().getMapRegion();
					if (region == null) {
						// some npc without region !!!
					}
					else {
						List<ZoneInstance> zones = region.getZones(npc);
						for (ZoneInstance zone : zones) {
							townId = zone.getTownId();
							if (townId > 0)
								break;
						}
						writeH(townId);
					}
				}
			}
		}
		else
			writeH(0);
	}

}
