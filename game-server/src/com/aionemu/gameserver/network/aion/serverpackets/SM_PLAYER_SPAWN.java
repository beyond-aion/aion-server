package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * Tells the client which world to load and where to put the player.
 *
 * @author -Nemesiss-
 */
public class SM_PLAYER_SPAWN extends AionServerPacket {

	private final Player player;

	public SM_PLAYER_SPAWN(Player player) {
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(player.getPosition().getClientWorldId()); // the world with the channel
		writeD(player.getWorldId()); // the map without the channel
		writeD(0); // todo: world+0x28, meaning unknown
		writeC(WorldMapType.getWorld(player.getWorldId()).isPersonal() ? 1 : 0); // world kind: 1 for a housing world
		writeF(player.getX());
		writeF(player.getY());
		writeF(player.getZ());
		writeC(player.getHeading());
		writeD(0); // todo: the server-wide scene status (the builder's show/set_global_scene_status), the emulator has none
		writeD(0); // todo: world+0x183CC in retail, 100000 in some worlds
		writeD(0); // todo: a world property from its vtable, a time attack world value (values 1-12 display different buff icons) 
		writeC(0); // 1 on Fast-Track server, not implemented in emulator
		writeD(0); // todo: a world property from its vtable, a time attack world value
		writeC(0); // todo: World::GetTribeConflict, not implemented
	}
}
