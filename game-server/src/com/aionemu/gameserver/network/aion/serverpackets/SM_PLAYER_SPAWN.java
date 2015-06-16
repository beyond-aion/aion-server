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

	/**
	 * Constructor.
	 *
	 * @param player
	 */
	public SM_PLAYER_SPAWN(Player player) {
		super();
		this.player = player;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(player.getWorldId() + player.getInstanceId() - 1); // world + chnl
		writeD(player.getWorldId());
		writeD(0x00); // unk
		writeC(WorldMapType.getWorld(player.getWorldId()).isPersonal() ? 1 : 0);
		writeF(player.getX()); // x
		writeF(player.getY()); // y
		writeF(player.getZ()); // z
		writeC(player.getHeading()); // heading
		writeD(0); // new 2.5
		writeD(0); // new 2.5
		// 1 - Azphels Curse, too much low level player killed
		// 2 - Victorys Pledge, boost attack 15, magic boost by 95, max hp by 440, healing boost by 30
		// 3 - Victorys Pledge, boost attack 20, magic boost by 105, max hp by 520, healing boost by 30
		// 4 - Victorys Pledge, boost attack 20, magic boost by 115, max hp by 600, healing boost by 30
		// 5 - Victorys Pledge, boost attack 25, magic boost by 125, max hp by 680, healing boost by 50
		// 6 - Victorys Pledge, boost attack 25, magic boost by 125, max hp by 680, healing boost by 50
		// 7 - Boost Moral, boost pvp physical and magical defense by 90%, movement speed by 50%, magical resist by 9999, all altered state resist by 1000
		// 8 - Boost Moral, boost pvp physical and magical defense by 90%, movement speed by 50%, magical resist by 9999, all altered state resist by 1000
		// 9 - I never lose, boost pvp physical/magical attack/defense by 10%
		if (player.getSKInfo().getRank() > 0) {
			writeD(1);
		}  else {
			writeD(0);
		}
		if (World.getInstance().getWorldMap(player.getWorldId()).getTemplate().getBeginnerTwinCount() > 0)
			writeC(1);
		else
			writeC(0);
		writeD(0); // 4.0
		writeC(0); // 4.7
	}

}
