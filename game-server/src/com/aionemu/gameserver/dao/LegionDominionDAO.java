package com.aionemu.gameserver.dao;

import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;

/**
 * @author Yeats
 *
 */
public abstract class LegionDominionDAO implements DAO {

	@Override
	public final String getClassName() {
		return LegionDominionDAO.class.getName();
	}

	public abstract boolean loadLegionDominionLocations(Map<Integer, LegionDominionLocation> legionDominionLocations);

	public abstract boolean updateLegionDominionLocation(LegionDominionLocation loc);

	public abstract Map<Integer, LegionDominionParticipantInfo> loadParticipants(LegionDominionLocation loc);

	public abstract void storeNewInfo(int id, LegionDominionParticipantInfo info);
	
	public abstract void updateInfo(LegionDominionParticipantInfo info);
}
