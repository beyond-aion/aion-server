package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * This packet is notify client what map should be loaded.
 *
 * @author -Nemesiss-
 */
public class SM_PLAYER_SPAWN extends AionServerPacket {

	/**
	 * Player that is entering game.
	 */
	private final Player player;

	public SM_PLAYER_SPAWN(Player player) {
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		boolean isPersonal = WorldMapType.getWorld(player.getWorldId()).isPersonal();
		int worldChannel = player.getWorldId() + player.getInstanceId() - 1;
		writeD(isPersonal ? -worldChannel : worldChannel); // world + chnl
		writeD(player.getWorldId());
		writeD(0x00); // unk
		writeC(isPersonal ? 1 : 0);
		writeF(player.getX()); // x
		writeF(player.getY()); // y
		writeF(player.getZ()); // z
		writeC(player.getHeading()); // heading
		writeD(0); // new 2.5
		writeD(0); // new 2.5
		// 1 - Azphels Curse, too much low level player killed (no longer implemented in client, since removal of serial killer system in 4.8)
		// 2 - Victorys Pledge, boost attack 15, magic boost by 95, max hp by 440, healing boost by 30
		// 3 - Victorys Pledge, boost attack 20, magic boost by 105, max hp by 520, healing boost by 30
		// 4 - Victorys Pledge, boost attack 20, magic boost by 115, max hp by 600, healing boost by 30
		// 5 - Victorys Pledge, boost attack 25, magic boost by 125, max hp by 680, healing boost by 50
		// 6 - Victorys Pledge, boost attack 25, magic boost by 125, max hp by 680, healing boost by 50
		// 7 - Boost Moral, [Arena] boost pvp physical and magical defense by 90%, movement speed by 50%, magical resist by 9999, all altered state resist by 1000
		// 8 - Boost Moral, [Arena] same stats like 7
		// 9 - I never lose, boost pvp physical/magical attack/defense by 10%
		// 10 - Boost Moral, [Kamar Battlefield] same stats like 7, except 100% pvp defense
		// 11 - Boost Moral, [Engulfed Ophidan Bridge] same stats like 7, except 100% pvp defense
		// 12 - Boost Moral, [Iron Wall War Front] same stats like 7, except 100% pvp defense
		writeD(0);
		if (World.getInstance().getWorldMap(player.getWorldId()).getTemplate().getBeginnerTwinCount() > 0)
			writeC(1);
		else
			writeC(0);
		writeD(0); // 4.0
		writeC(0); // 4.7
	}

}
