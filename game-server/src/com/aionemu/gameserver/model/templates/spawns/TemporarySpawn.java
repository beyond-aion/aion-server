package com.aionemu.gameserver.model.templates.spawns;

import java.time.DayOfWeek;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.utils.time.gametime.GameTime;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TemporarySpawn")
public class TemporarySpawn {

	@XmlAttribute(name = "weekdays")
	private List<DayOfWeek> weekdays;

	@XmlAttribute(name = "spawn_time")
	private String spawnTime; // *.*.* hour.day.month (* = all, /n = every nth hour/day/month starting from 0)

	@XmlAttribute(name = "despawn_time")
	private String despawnTime; // *.*.* hour.day.month (* = all, /n = every nth hour/day/month starting from 0)

	private Integer spawnHour;
	private Integer spawnDay;
	private Integer spawnMonth;
	private Integer despawnHour;
	private Integer despawnDay;
	private Integer despawnMonth;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (spawnTime != null) {
			spawnHour = parseTime(spawnTime, 0);
			spawnDay = parseTime(spawnTime, 1);
			spawnMonth = parseTime(spawnTime, 2);
			spawnTime = null;
		}
		if (despawnTime != null) {
			despawnHour = parseTime(despawnTime, 0);
			despawnDay = parseTime(despawnTime, 1);
			despawnMonth = parseTime(despawnTime, 2);
			despawnTime = null;
		}
	}

	private Integer parseTime(String time, int type) {
		String result = time.split("\\.")[type];
		if (result.equals("*"))
			return null;
		if (result.startsWith("/"))
			result = "-" + result.substring(1); // parse negative for later expression handling (/2 means every 2 hours)
		return Integer.parseInt(result);
	}

	private boolean isTime(Integer hour, Integer day, Integer month) {
		GameTime gameTime = GameTimeService.getInstance().getGameTime();
		if (hour != null) {
			int gameTimeHour = gameTime.getHour();
			if (hour >= 0 && gameTimeHour != hour || hour < 0 && gameTimeHour % hour != 0)
				return false;
		}
		if (day != null) {
			int gameTimeDay = gameTime.getDay();
			if (day >= 0 && gameTimeDay != day || day < 0 && gameTimeDay % day != 0)
				return false;
		}
		if (month != null) {
			int gameTimeMonth = gameTime.getMonth();
			if (month >= 0 && gameTimeMonth != month || month < 0 && gameTimeMonth % month != 0)
				return false;
		}
		return true;
	}

	public boolean canSpawn() {
		if (weekdays != null && !weekdays.isEmpty() && !weekdays.contains(ServerTime.now().getDayOfWeek()))
			return false;
		return isTime(spawnHour, spawnDay, spawnMonth);
	}

	public boolean canDespawn() {
		if (weekdays != null && !weekdays.isEmpty() && !weekdays.contains(ServerTime.now().getDayOfWeek()))
			return true;
		return isTime(despawnHour, despawnDay, despawnMonth);
	}

	public boolean isInSpawnTime() {
		if (weekdays != null && !weekdays.isEmpty() && !weekdays.contains(ServerTime.now().getDayOfWeek()))
			return false;

		GameTime gameTime = GameTimeService.getInstance().getGameTime();

		if (spawnMonth != null && !checkDate(gameTime.getMonth(), spawnMonth, despawnMonth))
			return false;

		if (spawnDay != null && !checkDate(gameTime.getDay(), spawnDay, despawnDay))
			return false;

		if (spawnHour != null && !checkHour(gameTime.getHour(), spawnHour, despawnHour))
			return false;

		return true;
	}

	private boolean checkDate(int currentDate, int spawnDate, Integer despawnDate) {
		if (despawnDate != null && despawnDate < 0) // check "every nth month/day" expression
			return checkWithDespawnExpression(currentDate, spawnDate, -despawnDate);

		if (spawnDate < 0)
			spawnDate = -spawnDate; // make the expression a positive spawn time, works just fine
		if (despawnDate == null) // any date
			return currentDate >= spawnDate;
		if (spawnDate <= despawnDate)
			return currentDate >= spawnDate && currentDate <= despawnDate;
		else
			return currentDate >= spawnDate || currentDate <= despawnDate;
	}

	private boolean checkHour(int currentHour, int spawnHour, Integer despawnHour) {
		if (despawnHour != null && despawnHour < 0) // check "every nth month/day" expression
			return checkWithDespawnExpression(currentHour, spawnHour, -despawnHour);

		if (spawnHour < 0)
			spawnHour = -spawnHour; // make the expression a positive spawn time, works just fine
		if (despawnHour == null) // any hour
			return currentHour >= spawnHour;
		if (spawnHour < despawnHour)
			return currentHour >= spawnHour && currentHour < despawnHour;
		if (spawnHour > despawnHour)
			return currentHour >= spawnHour || currentHour < despawnHour;
		return true;
	}

	private boolean checkWithDespawnExpression(int currentDate, int spawnTimeOrExpression, int despawnExpression) {
		// proper handling would be really complex, so for now some spawn/despawn combinations don't spawn directly on server start
		if (spawnTimeOrExpression < 0) // change expression to time
			spawnTimeOrExpression = -spawnTimeOrExpression;
		return currentDate >= spawnTimeOrExpression && spawnTimeOrExpression == despawnExpression;
	}
}
