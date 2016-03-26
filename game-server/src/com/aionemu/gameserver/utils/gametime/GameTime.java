package com.aionemu.gameserver.utils.gametime;

import java.security.InvalidParameterException;

import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.spawnengine.TemporarySpawnEngine;

/**
 * Represents the internal clock for the time in aion world
 * 
 * @author Ben
 * @reworked vlog
 * @modified Neon
 */
public class GameTime implements Cloneable {

	private static final int MINUTES_IN_HOUR = 60;
	private static final int MINUTES_IN_DAY = MINUTES_IN_HOUR * 24;
	private static final int MINUTES_IN_YEAR = (31 * 12) * MINUTES_IN_DAY;
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
	};

	/**
	 * Constructs a GameTime with the given time in minutes since midnight 01.01.0000
	 * 
	 * @param time
	 *          Minutes since midnight 01.01.0000
	 */
	public GameTime(int time) {
		if (time < 0)
			throw new InvalidParameterException("Time must be >= 0");
		gameTime = time;
		dayTime = calculateDayTime();
	}

	/**
	 * Gets the ingame time in minutes
	 * 
	 * @return The number of minutes since 01.01.0000 00:00
	 */
	public int getTime() {
		return gameTime;
	}

	/**
	 * Increases game time by a minute
	 */
	public void addMinutes(int minutes) {
		gameTime += minutes;
		onTimeChange();
	}

	/**
	 * @return the dayTime
	 */
	public DayTime getDayTime() {
		return dayTime;
	}

	public boolean setDayTime(DayTime dayTime) {
		if (this.dayTime == dayTime)
			return false;
		this.dayTime = dayTime;
		return true;
	}

	/**
	 * Gets the year in the game: 0 - <integer bound>
	 * 
	 * @return Year
	 */
	public int getYear() {
		return gameTime / MINUTES_IN_YEAR;
	}

	/**
	 * Gets the month in the game, 1 - 12
	 * 
	 * @return Month 1-12
	 */
	public int getMonth() {
		int answer = 1;
		int minutesInYear = gameTime % MINUTES_IN_YEAR;
		for (Month m : Month.values()) {
			if ((minutesInYear - getProperMinutesInMonth(m)) > 0) {
				minutesInYear = minutesInYear - getProperMinutesInMonth(m);
				answer = answer + 1;
			} else if ((minutesInYear - getProperMinutesInMonth(m)) == 0) {
				answer = answer + 1;
				break;
			} else {
				break;
			}
		}
		return answer;
	}

	/**
	 * Get the proper amount of minutes in this month
	 * 
	 * @param m
	 * @return time in minutes in this month
	 */
	public int getProperMinutesInMonth(Month m) {
		return m.getDays() * MINUTES_IN_DAY;
	}

	/**
	 * Gets the day in the game, 1 - Month.getDays()
	 * 
	 * @return Day 1 - Month.getDays()
	 */
	public int getDay() {
		int answer = 1;
		int minutesInYear = gameTime % MINUTES_IN_YEAR;
		for (Month m : Month.values()) {
			if ((minutesInYear - getProperMinutesInMonth(m)) > 0) {
				minutesInYear = minutesInYear - getProperMinutesInMonth(m);
			} else if ((minutesInYear - getProperMinutesInMonth(m)) == 0) {
				break;
			} else {
				answer = minutesInYear / MINUTES_IN_DAY + 1;
				break;
			}
		}
		return answer;
	}

	/**
	 * Gets the hour in the game, 0-23
	 * 
	 * @return Hour 0-23
	 */
	public int getHour() {
		return (gameTime % MINUTES_IN_DAY) / MINUTES_IN_HOUR;
	}

	/**
	 * Gets the minute in the game, 0-59
	 * 
	 * @return Minute 0-59
	 */
	public int getMinute() {
		return gameTime % MINUTES_IN_HOUR;
	}

	/**
	 * Perform actions upon time change
	 */
	public void onTimeChange() {
		if (getMinute() == 0)
			onHourChange();
		if (setDayTime(calculateDayTime()))
			onDayTimeChange();
	}

	private void onHourChange() {
		TemporarySpawnEngine.onHourChange();
	}

	private void onDayTimeChange() {
		WeatherService.getInstance().checkWeathersTime();
	}

	/**
	 * @return The calculated day time based on the current game hour.
	 */
	public DayTime calculateDayTime() {
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

	/**
	 * Convert from game time into real time
	 * 
	 * @author vlog
	 */
	public int convertTime() {
		return this.getTime() / 12;
	}

	/**
	 * Subtract the given game time from this game time
	 * 
	 * @param game
	 *          time to subtract
	 * @return new game time
	 */
	public GameTime minus(GameTime gt) {
		return new GameTime(this.getTime() - gt.getTime());
	}

	/**
	 * Add the given game time to this game time
	 * 
	 * @param game
	 *          time to add
	 * @return new game time
	 */
	public GameTime plus(GameTime gt) {
		return new GameTime(this.getTime() + gt.getTime());
	}

	/**
	 * Compares this time and the time given
	 * 
	 * @param gt
	 * @return true, if this time is greater
	 */
	public boolean isGreaterThan(GameTime gt) {
		return this.getTime() > gt.getTime();
	}

	/**
	 * Compares this time and the time given
	 * 
	 * @param gt
	 * @return true, if this time is less
	 */
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
		if (this != obj)
			return true;
		if (!(obj instanceof GameTime))
			return false;
		return gameTime == ((GameTime) obj).gameTime;
	}

	@Override
	public Object clone() {
		return new GameTime(gameTime);
	}
}
