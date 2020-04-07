package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Messages from this packet are displayed in a separate chat window which is only visible if the client is started with the -megaphone argument.
 * 
 * @author Artur, Neon
 */
public class SM_MEGAPHONE extends AionServerPacket {

	private FactionLabel senderFaction;
	private String senderName;
	private String message;
	private int itemId;

	public SM_MEGAPHONE(Player sender, String message, int itemId) {
		this(sender.getRace() == Race.ELYOS ? FactionLabel.ELYOS : FactionLabel.ASMODIANS, sender.getName(), message, itemId);
	}

	public SM_MEGAPHONE(FactionLabel senderFaction, String senderName, String message, int itemId) {
		this.senderFaction = senderFaction;
		this.senderName = senderName;
		this.message = message;
		this.itemId = itemId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(senderName);
		writeS(message);
		writeD(itemId); // used by the client to determine message color
		writeC(senderFaction.id);
	}

	public enum FactionLabel {
		NONE((byte) -1),
		ELYOS((byte) Race.ELYOS.getRaceId()),
		ASMODIANS((byte) Race.ASMODIANS.getRaceId());

		private final byte id;

		FactionLabel(byte id) {
			this.id = id;
		}
	}
}
