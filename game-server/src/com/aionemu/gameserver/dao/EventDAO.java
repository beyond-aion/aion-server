package com.aionemu.gameserver.dao;

import java.util.List;
import java.util.Set;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author Neon
 */
public abstract class EventDAO implements DAO {

	@Override
	public String getClassName() {
		return EventDAO.class.getName();
	}

	/**
	 * Called once before event start to delete outdated buff data.
	 */
	public abstract void deleteOldBuffData();

	/**
	 * Called during event service start for each event.
	 */
	public abstract List<StoredBuffData> loadStoredBuffData(String eventName);

	/**
	 * Called every time when buff data changes for the an active event.
	 */
	public abstract boolean storeBuffData(String eventName, List<StoredBuffData> buffData);

	public static class StoredBuffData {

		private final int buffIndex;
		private final Set<Integer> activePoolSkillIds;
		private final Set<Integer> allowedBuffDays;

		public StoredBuffData(int buffIndex, Set<Integer> activePoolSkillIds, Set<Integer> allowedBuffDays) {
			this.buffIndex = buffIndex;
			this.activePoolSkillIds = activePoolSkillIds;
			this.allowedBuffDays = allowedBuffDays;
		}

		public int getBuffIndex() {
			return buffIndex;
		}

		public Set<Integer> getActivePoolSkillIds() {
			return activePoolSkillIds;
		}

		public Set<Integer> getAllowedBuffDays() {
			return allowedBuffDays;
		}
	}
}
