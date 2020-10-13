package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class SM_ITEM_USAGE_ANIMATION extends AionServerPacket {

	private int playerObjId;
	private int targetObjId;
	private int itemObjId;
	private int itemId;
	private int time;
	private int end;
	private int unk;
	private int unk1;
	private int unk2 = 1;
	private int unk3;

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId) {
		this.playerObjId = playerObjId;
		this.targetObjId = playerObjId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.time = 0;
		this.end = 1;
		this.unk3 = 1;
	}

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId, int time, int end) {
		this.playerObjId = playerObjId;
		this.targetObjId = playerObjId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.time = time;
		this.end = end;
	}

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId, int time, int end, int unk) {
		this.playerObjId = playerObjId;
		this.targetObjId = playerObjId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.time = time;
		this.end = end;
		this.unk3 = unk;
	}

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int targetObjId, int itemObjId, int itemId, int time, int end, int unk) {
		this.playerObjId = playerObjId;
		this.targetObjId = targetObjId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.time = time;
		this.end = end;
		this.unk3 = unk;
	}

	public SM_ITEM_USAGE_ANIMATION(int playerObjId, int targetObjId, int itemObjId, int itemId, int time, int end, int unk, int unk1, int unk2, int unk3) {
		this.playerObjId = playerObjId;
		this.targetObjId = targetObjId;
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.time = time;
		this.end = end;
		this.unk = unk;
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (time > 0) {
			final Player player = World.getInstance().getPlayer(playerObjId);
			final Item item = player.getInventory().getItemByObjId(itemObjId);
			player.setUsingItem(item);
		}

		writeD(playerObjId); // player obj id
		writeD(targetObjId); // target obj id

		writeD(itemObjId); // itemObjId
		writeD(itemId); // item id

		writeD(time); // unk
		writeC(end); // unk
		writeC(unk); // unk
		writeC(unk1);
		writeC(unk2);// unk
		writeD(unk3);// mb cd?
	}
}
