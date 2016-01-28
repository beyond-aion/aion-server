package com.aionemu.gameserver.model.legionDominion;

import java.sql.Timestamp;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Yeats
 *
 */
public class LegionDominionParticipantInfo implements Comparable<LegionDominionParticipantInfo> {

	private int legionId;
	private int points;
	private int time;
	private Timestamp date;

	/**
	 * @return legionId
	 */
	public int getLegionId() {
		return legionId;
	}
	
	/**
	 * @return points
	 */
	public int getPoints() {
		return points;
	}
	
	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
	
	/**
	 * @return the date as a unix timestamp
	 */
	public long getDate() {
		return date != null ? date.getTime()/1000 : 0;
	}
	
	public Timestamp getDateAsTimeStamp() {
		return date;
	}

	/**
	 * @param legionId
	 */
	public void setLegionId(int legionId) {
		this.legionId = legionId;
	}
	
	/**
	 * @param points the points to set
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * @param timestamp the date to set
	 */
	public void setDate(Timestamp timestamp) {
		this.date = timestamp;
	}

	@Override
	public int compareTo(LegionDominionParticipantInfo info) {
		if (info.getPoints() == points) {
			if (info.getTime() == time) { //points same? sort by needed time
				if (info.getDate() == getDate()) { //time same? sort by participated date
					return 0;
				} else if (info.getDate() > getDate()) {
					return -1;
				} else {
					return 1;
				}
			} else if (info.getTime() > time) {
				return -1;
			} else {
				return 1;
			}
		} else if (info.getPoints() > points) {		
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * @return Name of this Legion
	 */
	public String getLegionName() {
		Legion legion = LegionService.getInstance().getLegion(legionId);
		if (legion != null)
			return legion.getLegionName();
		return "NOT AVAILABLE";
	}
}
