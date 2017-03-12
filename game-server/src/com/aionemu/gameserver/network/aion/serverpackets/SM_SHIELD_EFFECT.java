package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.Collection;

import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author xTz, Source
 */
public class SM_SHIELD_EFFECT extends AionServerPacket {

	private Collection<SiegeLocation> locations;

	public SM_SHIELD_EFFECT(Collection<SiegeLocation> locations) {
		this.locations = locations;
	}

	public SM_SHIELD_EFFECT(int location) {
		this.locations = new ArrayList<>();
		this.locations.add(SiegeService.getInstance().getSiegeLocation(location));
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(locations.size());
		for (SiegeLocation loc : locations) {
			writeD(loc.getLocationId());
			writeC(loc.isUnderShield() ? 1 : 0);
		}
	}

}
