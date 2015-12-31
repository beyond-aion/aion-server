package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;

import javolution.util.FastMap;

/**
 * @author Sarynth
 * @modified Neon
 */
public class SM_SIEGE_LOCATION_INFO extends AionServerPacket {

	/**
	 * infoType 0 - reset 1 - update
	 */
	private int infoType;
	private Map<Integer, SiegeLocation> locations;

	public SM_SIEGE_LOCATION_INFO() {
		this.infoType = 0;
		locations = SiegeService.getInstance().getSiegeLocations();
	}

	public SM_SIEGE_LOCATION_INFO(SiegeLocation loc) {
		this.infoType = 1;
		locations = new FastMap<Integer, SiegeLocation>();
		locations.put(loc.getLocationId(), loc);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		if (!SiegeConfig.SIEGE_ENABLED) {
			writeC(0);
			writeH(0);
			return;
		} 

		if (player.getPanesterraTeam() != null && player.getPanesterraTeam().getTeamId().getId() >= 69 
				&& player.getPanesterraTeam().getTeamId().getId() <= 72) {
			writeC(0);
			writeH(4); //4 Panesterra Fortresses
			
			int fortressId = player.getPanesterraTeam().getFortressId();
			Race playerRace = player.getRace();
			for (int i=0; i < 4; i++) {
				int curFortress = 10111 + (i*100); // 10111, 10211, 10311, 10411
				writeD(curFortress);
				writeD(0); //belongs to no legion
				//default emblem id & color
				writeD(0x00);
				writeC(255);
				writeC(0x00);
				writeC(0x00);
				writeC(0x00);
					
				writeC(curFortress == fortressId ? playerRace.getRaceId() : playerRace == Race.ASMODIANS ? Race.ELYOS.getRaceId() : Race.ASMODIANS.getRaceId());
				writeC(0); //not vulnerable
				writeC(0); //cannot teleport
				writeC(0); //next state invulnerable
					
				writeH(0); 
				writeH(0);
				writeD(0);
					
				writeD(NetworkConfig.GAMESERVER_ID); //serverId, we dont have to save it since theres only 1 server running
				writeD(0);
				writeD(0);
			}
		} else {
			writeC(infoType);
			writeH(locations.size());

		for (SiegeLocation loc : locations.values()) {
			LegionEmblem emblem = new LegionEmblem();
			int legionId = loc.getLegionId();
			int locId = loc.getLocationId();
			writeD(locId);
			writeD(legionId);
			if (legionId != 0 && LegionService.getInstance().getLegion(legionId) != null) // can be null if legion got deleted
				emblem = LegionService.getInstance().getLegion(legionId).getLegionEmblem();
			writeC(emblem.getEmblemId());
			writeC(emblem.getEmblemType().getValue());
			writeH(0);
			writeC(emblem.getColor_a());
			writeC(emblem.getColor_r());
			writeC(emblem.getColor_g());
			writeC(emblem.getColor_b());
			writeC(loc.getRace().getRaceId());
			writeC(loc.isVulnerable() ? 2 : 0); // is vulnerable (0 - no, 2 - yes)
			writeC(loc.isCanTeleport(player) ? 1 : 0);
			writeC(loc.getNextState()); // Next State (0 - invulnerable, 1 - vulnerable)
			writeH(0); // unk
			writeH(0);
			writeD(locId == 2111 || locId == 3111 ? SiegeService.getInstance().getRemainingSiegeTimeInSeconds(locId) : 0); // veille/masta timer
			writeD(NetworkConfig.GAMESERVER_ID); // server ID of the fortress owner (TODO relevant for panesterra, so change this later)
			writeD(0); // unk 4.7 (some timestamp, maybe Capture Date?)
			writeD(loc.getOccupiedCount());
		}
	}
	}
}
