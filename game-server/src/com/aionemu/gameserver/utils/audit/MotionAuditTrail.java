package com.aionemu.gameserver.utils.audit;

/**
 * The last events of a player that the motion gates check, kept to explain an audit entry afterwards: what came before it, when, and which gate
 * each step set. Primitive arrays, so recording allocates nothing.
 */
public class MotionAuditTrail {

	/**
	 * Each value is named "name" or "name:unit"; the unit "m" marks a distance stored in decimetres
	 */
	public enum Event {
		/** a skill cast ended and set the gates */
		CAST("skill", "cast:ms", "client hit time:ms", "hit time:ms", "next skill in:ms", "next auto attack in:ms"),
		/** a skill arrived before the gate allowed it */
		SKILL_TOO_EARLY("skill", "too early:ms", "previous skill"),
		/** an auto attack went through, the interval is -1 for the first one */
		AUTO_ATTACK("interval:ms", "head start:ms", "client hit time:ms", "hit time:ms", "animation", "distance:m"),
		/** an auto attack arrived before the animation of the last skill allowed it */
		AUTO_ATTACK_BEFORE_SKILL("too early:ms", "last skill"),
		/** an auto attack came faster than the attack speed allows */
		AUTO_ATTACK_TOO_FAST("interval:ms", "head start:ms"),
		/** an auto attack of the summon */
		SUMMON_ATTACK("interval:ms", "client hit time:ms", "hit time:ms", "accepted"),
		/** the player jumped */
		JUMP,
		/** the player came down from a jump or a fall, as far as the movement packets tell */
		LAND;

		private final String[] valueNames;

		Event(String... valueNames) {
			this.valueNames = valueNames;
		}
	}

	/** Bits of the flags field */
	public static final int MOVING = 1, MECH = 2, HIT_TIME_BOOSTED = 4;

	private static final int SIZE = 32;
	private static final int VALUES = 6;
	private static final int FIELDS = 3 + VALUES; // event, flags, attack speed, values

	private final long[] times = new long[SIZE];
	private final int[] fields = new int[SIZE * FIELDS];
	private int next;
	private int count;
	private long lastDumpMillis;
	private long lastJumpMillis;
	private long lastLandMillis;

	public synchronized void add(Event event, int flags, int attackSpeed, int value1, int value2, int value3, int value4, int value5, int value6) {
		int i = next * FIELDS;
		times[next] = System.currentTimeMillis();
		fields[i] = event.ordinal();
		fields[i + 1] = flags;
		fields[i + 2] = attackSpeed;
		fields[i + 3] = value1;
		fields[i + 4] = value2;
		fields[i + 5] = value3;
		fields[i + 6] = value4;
		fields[i + 7] = value5;
		fields[i + 8] = value6;
		if (event == Event.JUMP)
			lastJumpMillis = times[next];
		else if (event == Event.LAND)
			lastLandMillis = times[next];
		next = (next + 1) % SIZE;
		if (count < SIZE)
			count++;
	}

	/**
	 * @return The events from the oldest to the newest, one per line, with times relative to now, or null if the last dump was less than
	 *         minIntervalMillis ago
	 */
	public synchronized String dump(long minIntervalMillis) {
		long now = System.currentTimeMillis();
		if (now - lastDumpMillis < minIntervalMillis)
			return null;
		lastDumpMillis = now;
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < count; n++) {
			int slot = (next - count + n + SIZE) % SIZE;
			int i = slot * FIELDS;
			Event event = Event.values()[fields[i]];
			sb.append("\n  ").append(times[slot] - now).append(" ms ").append(event).append(event.valueNames.length > 0 ? ":" : "");
			for (int v = 0; v < event.valueNames.length; v++) {
				String[] nameAndUnit = event.valueNames[v].split(":");
				int value = fields[i + 3 + v];
				sb.append(v == 0 ? " " : ", ").append(nameAndUnit[0]).append(' ');
				if (nameAndUnit.length == 1)
					sb.append(value);
				else if (nameAndUnit[1].equals("m"))
					sb.append(value / 10f).append(" m");
				else
					sb.append(value).append(' ').append(nameAndUnit[1]);
			}
			sb.append(" | attack speed ").append(fields[i + 2]).append(" ms");
			int flags = fields[i + 1];
			if ((flags & MOVING) != 0)
				sb.append(", moving");
			if ((flags & MECH) != 0)
				sb.append(", mech");
			if ((flags & HIT_TIME_BOOSTED) != 0)
				sb.append(", hit time boosted");
		}
		return sb.toString();
	}

	/**
	 * @return How long ago the player jumped and landed, if within the given time, or null
	 */
	public synchronized String describeRecentJump(long withinMillis) {
		long now = System.currentTimeMillis();
		if (now - lastJumpMillis > withinMillis)
			return null;
		return "jumped " + (now - lastJumpMillis) + " ms" + (lastLandMillis >= lastJumpMillis ? " and landed " + (now - lastLandMillis) + " ms" : "") + " before";
	}
}
