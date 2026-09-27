package com.aionemu.gameserver.utils.time.gametime;

import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.spawnengine.TemporarySpawnEngine;

/**
 * Represents the internal clock for the time in aion world
 * 
 * @author Ben, vlog, Neon
 */
public class GameTime implements Cloneable {

	private static final int MINUTES_IN_HOUR = 60;
	private static final int MINUTES_IN_DAY = MINUTES_IN_HOUR * 24;
	private static final int MINUTES_IN_YEAR = Month.getDaysOfYear() * MINUTES_IN_DAY;
	private int gameTime;
	private DayTime dayTime;

	private enum Month {
		JANUARY(31),
		FEBRUARY(31),
		MARCH(31),
		APRIL(31),
		MAY(31),
		JUNE(31),
		JULY(31),
		AUGUST(31),
		SEPTEMBER(31),
		OCTOBER(31),
		NOVEMBER(31),
		DECEMBER(31);

		private int days;

		Month(int days) {
			this.days = days;
		}

		public int getDays() {
			return days;
		}

		public static int getDaysOfYear() {
			int daysOfYear = 0;
			for (Month m : values())
				daysOfYear += m.getDays();
			return daysOfYear;
		}
	};

	/**
	 * Constructs a GameTime with the given time in minutes since midnight 01.01.0000
	 * 
	 * @param time
	 *          Minutes since midnight 01.01.0000
	 */
	public GameTime(Integer time) {
		if (time != null && time < 0)
			throw new IllegalArgumentException("Time must be >= 0");
		gameTime = time == null ? 0 : time;
		dayTime = calculateDayTime();
	}

	/**
	 * @return The number of minutes since 01.01.0000 00:00
	 */
	public int getTime() {
		return gameTime;
	}

	/**
	 * Adds the given number of minutes to the GameTime
	 */
	public void addMinutes(int minutes) {
		if (minutes != 0) {
			gameTime += minutes;
			if (getMinute() == 0)
				onHourChange(minutes == 1);
		}
	}

	public DayTime getDayTime() {
		return dayTime;
	}

	/**
	 * @return True if the daytime changed
	 */
	public boolean setDayTime(DayTime dayTime) {
		if (this.dayTime == dayTime)
			return false;
		this.dayTime = dayTime;
		return true;
	}

	/**
	 * @return The year in the game, 0-4008 (since gameTime is int based)
	 */
	public int getYear() {
		return gameTime / MINUTES_IN_YEAR;
	}

	/**
	 * @return The number of the current month in the game, ranging from 1 to 12
	 */
	public int getMonth() {
		int month = 0;
		int minutesOfThisYear = gameTime % MINUTES_IN_YEAR;
		for (Month m : Month.values()) {
			month += 1;
			if ((minutesOfThisYear -= m.getDays() * MINUTES_IN_DAY) < 0)
				break;
		}
		return month;
	}

	/**
	 * @return The number of the day of month in the game, ranging from 1 to Month.getDays()
	 */
	public int getDay() {
		int day = 1;
		int minutesInYear = gameTime % MINUTES_IN_YEAR;
		for (Month m : Month.values()) {
			int minutesInMonth = m.getDays() * MINUTES_IN_DAY;
			if (minutesInYear > minutesInMonth) {
				minutesInYear -= minutesInMonth;
			} else {
				if (minutesInYear < minutesInMonth) // if both are equal, it's day 1 of the following month
					day += minutesInYear / MINUTES_IN_DAY;
				break;
			}
		}
		return day;
	}

	/**
	 * @return The hour in the game, 0-23
	 */
	public int getHour() {
		return (gameTime % MINUTES_IN_DAY) / MINUTES_IN_HOUR;
	}

	/**
	 * @return The minute in the game, 0-59
	 */
	public int getMinute() {
		return gameTime % MINUTES_IN_HOUR;
	}

	private void onHourChange(boolean changedByClock) {
		TemporarySpawnEngine.onHourChange();
		if (setDayTime(calculateDayTime()) && changedByClock) // don't change weather if time was changed by admin
			WeatherService.getInstance().checkWeathersTime();
	}

	/**
	 * @return The calculated day time based on the current game hour.
	 */
	private DayTime calculateDayTime() {
		int hour = getHour();
		if (hour > 21 || hour < 4)
			return DayTime.NIGHT;
		else if (hour > 16)
			return DayTime.EVENING;
		else if (hour > 8)
			return DayTime.AFTERNOON;
		else
			return DayTime.MORNING;
	}

	public GameTime minus(GameTime gt) {
		return new GameTime(this.getTime() - gt.getTime());
	}

	public GameTime plus(GameTime gt) {
		return new GameTime(this.getTime() + gt.getTime());
	}

	public boolean isGreaterThan(GameTime gt) {
		return this.getTime() > gt.getTime();
	}

	public boolean isLessThan(GameTime gt) {
		return this.getTime() < gt.getTime();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + gameTime;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof GameTime))
			return false;
		return gameTime == ((GameTime) obj).gameTime;
	}

	@Override
	public GameTime clone() {
		return new GameTime(gameTime);
	}
}
