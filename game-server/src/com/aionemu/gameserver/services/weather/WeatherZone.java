package com.aionemu.gameserver.services.weather;

import java.util.List;
import java.util.Random;

import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.world.WeatherPhase;

/**
 * The weather of one zone of one world instance. An idle zone rolls once per game hour; a running episode moves
 * through its lead-in, main and tail phases at the game minute each of them ends.
 */
public class WeatherZone {

	private enum Stage {
		IDLE,
		SIGN,
		BODY,
		AFTER_BODY,
		REMAIN,
		END
	}

	private final List<WeatherEntry> candidates;

	private volatile WeatherEntry current;
	private volatile int nextEventMinute;

	private WeatherEntry episode;
	private Stage stage = Stage.IDLE;

	WeatherZone(List<WeatherEntry> candidates) {
		this.candidates = candidates;
	}

	/**
	 * @return The weather the client is shown, null for clear sky.
	 */
	public WeatherEntry getCurrent() {
		return current;
	}

	public int getCode() {
		WeatherEntry entry = current;
		return entry == null ? 0 : entry.getCode();
	}

	/**
	 * @return The game minute the next phase change happens at, -1 when no weather is running.
	 */
	public int getNextEventMinute() {
		return stage == Stage.IDLE ? -1 : nextEventMinute;
	}

	/**
	 * Rolls a new weather. It happens once per game hour and only when no weather is running: the first entry in
	 * priority order whose month and hour bits are set and whose probability played wins.
	 *
	 * @param month Game month, 0-11.
	 * @param hour  Game hour, 0-23.
	 * @return True if the visible weather changed.
	 */
	public boolean roll(int now, int month, int hour, Random random) {
		if (stage != Stage.IDLE || !tryStart(now, month, hour, random))
			return false;
		return advance(now, random);
	}

	/**
	 * Drops the running weather and rolls a new one right away, without waiting for an hour boundary. Used by the
	 * admin command.
	 */
	public boolean next(int now, int month, int hour, Random random) {
		boolean changed = false;
		if (stage != Stage.IDLE) {
			changed = apply(null);
			episode = null;
			stage = Stage.IDLE;
		}
		if (tryStart(now, month, hour, random))
			changed |= advance(now, random);
		return changed;
	}

	/**
	 * Applies every phase change due at or before {@code now}, including the ones which fall into the same minute:
	 * a zero duration moves the episode on immediately.
	 *
	 * @return True if the visible weather changed.
	 */
	public boolean advance(int now, Random random) {
		boolean changed = false;
		// A zero duration moves the episode on in the same minute; the cap only keeps a malformed data cycle
		// (a weather which refers back to itself) from hanging the game clock.
		for (int transitions = 0; stage != Stage.IDLE && nextEventMinute <= now; transitions++) {
			if (transitions > 100)
				break;
			switch (stage) {
				case SIGN -> {
					WeatherPhase sign = episode.getSign();
					changed |= apply(sign.getWeather());
					nextEventMinute = now + roundToTen(sign.getDuration() + random.nextInt(sign.getDurationEx() + 1));
					stage = Stage.BODY;
				}
				case BODY -> {
					int duration = episode.getDuration() + random.nextInt(episode.getDurationEx() + 1);
					if (duration > 0) {
						changed |= apply(episode);
						nextEventMinute = now + roundToTen(duration);
					}
					stage = Stage.AFTER_BODY;
				}
				case AFTER_BODY -> {
					WeatherPhase remain = episode.getRemain();
					if (remain != null && random.nextInt(100) < remain.getProbability()) {
						stage = Stage.REMAIN;
					} else {
						WeatherEntry sub = pickSubWeather(random);
						if (sub == null) {
							stage = Stage.END;
						} else {
							episode = sub;
							stage = Stage.BODY;
						}
					}
					nextEventMinute = now;
				}
				case REMAIN -> {
					WeatherPhase remain = episode.getRemain();
					changed |= apply(remain.getWeather());
					nextEventMinute = now + roundToTen(remain.getDuration() + random.nextInt(remain.getDurationEx() + 1));
					stage = Stage.END;
				}
				case END -> {
					changed |= apply(null);
					episode = null;
					stage = Stage.IDLE;
				}
				default -> throw new IllegalStateException("Unexpected stage " + stage);
			}
		}
		return changed;
	}

	/**
	 * Moves the pending phase change by {@code minutes}, so a running weather keeps the time it has left when the game
	 * clock jumps.
	 */
	public void shift(int minutes) {
		if (stage != Stage.IDLE)
			nextEventMinute += minutes;
	}

	/**
	 * Puts the given weather on this zone and drops the running episode, so the natural cycle can take over again at
	 * the next hour. Used by the admin command.
	 */
	public void setWeather(WeatherEntry entry) {
		current = entry;
		episode = null;
		stage = Stage.IDLE;
		nextEventMinute = 0;
	}

	private boolean tryStart(int now, int month, int hour, Random random) {
		for (WeatherEntry candidate : candidates) {
			if (!candidate.isDueIn(month, hour))
				continue;
			if (random.nextInt(100) >= candidate.getOccurProbability())
				continue;
			episode = candidate;
			int delay = roundToTen(random.nextInt(candidate.getOccurDelay() + 1));
			nextEventMinute = now + delay;
			WeatherPhase sign = candidate.getSign();
			stage = sign != null && random.nextInt(100) < sign.getProbability() ? Stage.SIGN : Stage.BODY;
			return true;
		}
		return false;
	}

	private WeatherEntry pickSubWeather(Random random) {
		for (WeatherPhase sub : episode.getSubWeathers())
			if (random.nextInt(100) < sub.getProbability())
				return sub.getWeather();
		return null;
	}

	private boolean apply(WeatherEntry entry) {
		WeatherEntry previous = current;
		current = entry;
		return previous != entry;
	}

	/**
	 * Every phase duration is rounded to the nearest 10 game minutes, half up.
	 */
	static int roundToTen(int minutes) {
		return 10 * ((minutes + 5) / 10);
	}
}
