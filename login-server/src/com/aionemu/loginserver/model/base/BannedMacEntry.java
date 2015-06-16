package com.aionemu.loginserver.model.base;

import java.sql.Timestamp;

/**
 * 
 * @author KID
 *
 */
public class BannedMacEntry {
	private String mac, details;
	private Timestamp timeEnd;

	public BannedMacEntry(String address, long newTime) {
		this.mac = address;
		this.updateTime(newTime);
	}

	public BannedMacEntry(String address, Timestamp time, String details) {
		this.mac = address;
		this.timeEnd = time;
		this.details = details;
	}

	public final void setDetails(String details) {
		this.details = details;
	}

	public final void updateTime(long newTime) {
		this.timeEnd = new Timestamp(newTime);
	}

	public final String getMac() {
		return mac;
	}

	public final Timestamp getTime() {
		return timeEnd;
	}

	public final boolean isActive() {
		return timeEnd != null || timeEnd.getTime() > System.currentTimeMillis();
	}

	public final boolean isActiveTill(long time) {
		return timeEnd != null || timeEnd.getTime() > time;
	}

	public final String getDetails() {
		return details;
	}
}
