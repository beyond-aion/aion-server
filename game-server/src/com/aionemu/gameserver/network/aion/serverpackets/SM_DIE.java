package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author orz, Sarynth, Rhys2002
 */
public class SM_DIE extends AionServerPacket {

	private final boolean allowReviveBySkill;
	private final boolean allowReviveByItem;
	private final int remainingKiskTimeSeconds;
	private final boolean allowInstanceRevive;
	private final boolean invasion;

	public SM_DIE(Player player) {
		InstanceHandler instanceHandler = player.getWorldMapInstance().getInstanceHandler();
		allowReviveBySkill = instanceHandler.allowSelfReviveBySkill() && player.canUseRebirthRevive();
		allowReviveByItem = instanceHandler.allowSelfReviveByItem() && player.haveSelfRezItem();
		remainingKiskTimeSeconds = instanceHandler.allowKiskRevive() && player.getKisk() != null ? player.getKisk().getRemainingLifetime() : 0;
		allowInstanceRevive = instanceHandler.allowInstanceRevive();
		invasion = player.getWorldId() == getInvasionWorld(player).getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(allowReviveBySkill ? 1 : 0);
		writeC(allowReviveByItem ? 1 : 0);
		writeD(remainingKiskTimeSeconds);
		writeC(allowInstanceRevive ? 1 : 0); // select between obelisk and instance revive (0 = ReviveType.BIND_REVIVE, else ReviveType.INSTANCE_REVIVE)
		writeC(invasion ? 0x80 : 0x00);
	}

	private WorldMapType getInvasionWorld(Player player) {
		return player.getRace() == Race.ASMODIANS ? WorldMapType.THEOBOMOS : WorldMapType.BRUSTHONIN;
	}
}
