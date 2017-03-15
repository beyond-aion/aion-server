package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.Collection;

import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.SiegeService;

public class SM_ABYSS_ARTIFACT_INFO3 extends AionServerPacket {

	private Collection<ArtifactLocation> locations;

	public SM_ABYSS_ARTIFACT_INFO3(Collection<ArtifactLocation> collection) {
		this.locations = collection;
	}

	public SM_ABYSS_ARTIFACT_INFO3(int loc) {
		locations = new ArrayList<>();
		locations.add(SiegeService.getInstance().getArtifact(loc));
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(locations.size());
		for (ArtifactLocation artifact : locations) {
			writeD(artifact.getLocationId() * 10 + 1);
			writeC(artifact.getStatus().getValue());
			writeD(0);
		}
	}

}
