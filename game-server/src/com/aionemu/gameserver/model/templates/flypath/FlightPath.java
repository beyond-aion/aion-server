package com.aionemu.gameserver.model.templates.flypath;

public class FlightPath {

	private static final float BOOST_SPEED = 1.5f;
	private static final int BOOST_COOLDOWN = 15000;
	/**
	 * Stricter than retail, which only logs progress jumps of more than 2 s between two updates: progress ahead of the time elapsed since the start
	 * by more than this many milliseconds is refused, so a client can't skip along the path. Remove the check to fall back to retail.
	 */
	private static final int MAX_PROGRESS_LEAD = 2000;

	private final Type type;
	private final int id;
	private int distance;
	private int boostStart = -1, boostEnd = -1, boostedTime;
	private final int startDistance;
	private final long startTime = System.currentTimeMillis();

	public FlightPath(Type type, int id, int distance) {
		this.type = type;
		this.id = id;
		this.distance = distance;
		this.startDistance = distance;
	}

	public Type getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * @return Milliseconds since the flight started.
	 */
	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}

	/**
	 * The client counts the progress in real time from the start, boosting doesn't speed it up.
	 * 
	 * @return True if the progress (in milliseconds) can have been reached by now.
	 */
	public boolean isProgressReachable(int progress) {
		return progress - startDistance <= System.currentTimeMillis() - startTime + MAX_PROGRESS_LEAD;
	}

	/**
	 * @return False if the last boost started less than {@link #BOOST_COOLDOWN} ms of progress ago.
	 */
	public boolean startBoost(int progress) {
		if (boostStart >= 0 && progress < boostStart + BOOST_COOLDOWN)
			return false;
		boostStart = progress;
		boostEnd = -1;
		return true;
	}

	public boolean endBoost(int progress) {
		if (boostStart < 0 || boostEnd >= 0)
			return false;
		boostEnd = progress;
		boostedTime += progress - boostStart;
		return true;
	}

	/**
	 * The client reports the progress of the unboosted flight, so the boosted time is added on top of it to get the actual position on the path.
	 * 
	 * @return The progress (in milliseconds) gained by boosting until the given progress.
	 */
	public int getBoostGain(int progress) {
		int time = boostStart < 0 || boostEnd >= 0 || progress <= boostStart ? boostedTime : boostedTime + progress - boostStart;
		return (int) (time * (BOOST_SPEED - 1));
	}

	public enum Type {
		FLIGHT_TRANSPORTER,
		WINDSTREAM
	}
}
