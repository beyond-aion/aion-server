package com.aionemu.gameserver.model.templates.spawns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.utils.time.gametime.GameTime;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TemporarySpawn")
public class TemporarySpawn {

	@XmlAttribute(name = "spawn_time")
	private String spawnTime; // *.*.* hour.day.month (* == all)

	@XmlAttribute(name = "despawn_time")
	private String despawnTime; // *.*.* hour.day.month (* == all)

	public Integer getSpawnHour() {
		return getTime(spawnTime, 0);
	}

	public Integer getSpawnDay() {
		return getTime(spawnTime, 1);
	}

	public Integer getSpawnMonth() {
		return getTime(spawnTime, 2);
	}

	public Integer getDespawnHour() {
		return getTime(despawnTime, 0);
	}

	public Integer getDespawnDay() {
		return getTime(despawnTime, 1);
	}

	public Integer getDespawnMonth() {
		return getTime(despawnTime, 2);
	}

	private Integer getTime(String time, int type) {
		String result = time.split("\\.")[type];
		return result.equals("*") ? null : Integer.parseInt(result);
	}

	private boolean isTime(Integer hour, Integer day, Integer month) {
		GameTime gameTime = GameTimeService.getInstance().getGameTime();
		if (hour != null && hour != gameTime.getHour())
			return false;
		if (day != null && day != gameTime.getDay())
			return false;
		if (month != null && month != gameTime.getMonth())
			return false;
		return true;
	}

	public boolean canSpawn() {
		return isTime(getSpawnHour(), getSpawnDay(), getSpawnMonth());
	}

	public boolean canDespawn() {
		return isTime(getDespawnHour(), getDespawnDay(), getDespawnMonth());
	}

	public boolean isInSpawnTime() {
		GameTime gameTime = GameTimeService.getInstance().getGameTime();

		Integer spawnMonth = getSpawnMonth();
		if (spawnMonth != null && !checkDate(gameTime.getMonth(), spawnMonth, getDespawnMonth()))
			return false;

		Integer spawnDay = getSpawnDay();
		if (spawnDay != null && !checkDate(gameTime.getDay(), spawnDay, getDespawnDay()))
			return false;

		if (spawnMonth == null && spawnDay == null && !checkHour(gameTime.getHour(), getSpawnHour(), getDespawnHour()))
			return false;

		return true;
	}

	private boolean checkDate(int currentDate, int spawnDate, int despawnDate) {
		if (spawnDate <= despawnDate)
			return currentDate >= spawnDate && currentDate <= despawnDate;
		else
			return currentDate >= spawnDate || currentDate <= despawnDate;
	}

	private boolean checkHour(int currentHour, int spawnHour, int despawnHour) {
		if (spawnHour < despawnHour)
			return currentHour >= spawnHour && currentHour < despawnHour;
		else if (spawnHour > despawnHour)
			return currentHour >= spawnHour || currentHour < despawnHour;
		return true;
	}

}
