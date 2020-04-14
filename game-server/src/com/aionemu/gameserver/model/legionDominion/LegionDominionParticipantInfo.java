package com.aionemu.gameserver.model.legionDominion;

import java.sql.Timestamp;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Yeats
 */
public class LegionDominionParticipantInfo {

	private int legionId;
	private int points;
	private int time;
	private Timestamp date;

	public int getLegionId() {
		return legionId;
	}

	public int getPoints() {
		return points;
	}

	public int getTime() {
		return time;
	}

	public long getDate() {
		return date != null ? date.getTime() / 1000 : 0;
	}

	public Timestamp getDateAsTimeStamp() {
		return date;
	}

	public void setLegionId(int legionId) {
		this.legionId = legionId;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setDate(Timestamp timestamp) {
		this.date = timestamp;
	}

	public String getLegionName() {
		Legion legion = LegionService.getInstance().getLegion(legionId);
		if (legion != null)
			return legion.getName();
		return "NOT AVAILABLE";
	}
}
