package com.aionemu.commons.utils.concurrent;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NB4L1
 */
public final class RunnableStatsManager {

	private static final Logger log = LoggerFactory.getLogger(RunnableStatsManager.class);
	/**
	 * Stats are recorded by any thread (packet execution, the network threads, thread pools), so both levels are concurrent maps.
	 */
	private static final Map<Class<?>, ClassStat> classStats = new ConcurrentHashMap<>();

	private static final class ClassStat {

		private final String className;
		private final MethodStat runnableStat;
		private final Map<String, MethodStat> methodStats = new ConcurrentHashMap<>();

		private ClassStat(Class<?> clazz) {
			className = clazz.getName().replace("com.aionemu.gameserver.", "");
			runnableStat = new MethodStat(className, "run()");
			methodStats.put("run()", runnableStat);
		}

		private MethodStat getRunnableStat() {
			return runnableStat;
		}

		private MethodStat getMethodStat(String methodName) {
			return methodStats.computeIfAbsent(methodName, name -> new MethodStat(className, name));
		}
	}

	private static final class MethodStat {

		private final String className;
		private final String methodName;

		private long count;
		private long total;
		private long min = Long.MAX_VALUE;
		private long max = Long.MIN_VALUE;

		private MethodStat(String className, String methodName) {
			this.className = className;
			this.methodName = methodName;
		}

		private synchronized void handleStats(long runTime) {
			count++;
			total += runTime;
			min = Math.min(min, runTime);
			max = Math.max(max, runTime);
		}
	}

	private static ClassStat getClassStat(Class<?> clazz) {
		return classStats.computeIfAbsent(clazz, ClassStat::new);
	}

	public static void handleStats(Class<? extends Runnable> clazz, long runTime) {
		getClassStat(clazz).getRunnableStat().handleStats(runTime);
	}

	public static void handleStats(Class<?> clazz, String methodName, long runTime) {
		getClassStat(clazz).getMethodStat(methodName).handleStats(runTime);
	}

	public static enum SortBy {
		AVG("average"),
		COUNT("count"),
		TOTAL("total"),
		NAME("class"),
		METHOD("method"),
		MIN("min"),
		MAX("max"), ;

		private final String xmlAttributeName;

		private SortBy(String xmlAttributeName) {
			this.xmlAttributeName = xmlAttributeName;
		}

		private final Comparator<MethodStat> comparator = new Comparator<MethodStat>() {

			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(MethodStat o1, MethodStat o2) {
				final Comparable c1 = getComparableValueOf(o1);
				final Comparable c2 = getComparableValueOf(o2);

				if (c1 instanceof Number)
					return c2.compareTo(c1);

				final String s1 = (String) c1;
				final String s2 = (String) c2;

				final int len1 = s1.length();
				final int len2 = s2.length();
				final int n = Math.min(len1, len2);

				for (int k = 0; k < n; k++) {
					char ch1 = s1.charAt(k);
					char ch2 = s2.charAt(k);

					if (ch1 != ch2) {
						if (Character.isUpperCase(ch1) != Character.isUpperCase(ch2))
							return ch2 - ch1;
						else
							return ch1 - ch2;
					}
				}

				final int result = len1 - len2;

				if (result != 0)
					return result;

				switch (SortBy.this) {
					case METHOD:
						return NAME.comparator.compare(o1, o2);
					default:
						return 0;
				}
			}
		};

		@SuppressWarnings("rawtypes")
		private Comparable getComparableValueOf(MethodStat stat) {
			switch (this) {
				case AVG:
					return stat.total / stat.count;
				case COUNT:
					return stat.count;
				case TOTAL:
					return stat.total;
				case NAME:
					return stat.className;
				case METHOD:
					return stat.methodName;
				case MIN:
					return stat.min;
				case MAX:
					return stat.max;
				default:
					throw new InternalError();
			}
		}

		private static final SortBy[] VALUES = SortBy.values();
	}

	public static void dumpClassStats() {
		dumpClassStats(null);
	}

	public static void dumpClassStats(final SortBy sortBy) {
		final List<MethodStat> methodStats = new ArrayList<>();

		for (ClassStat classStat : classStats.values())
			for (MethodStat methodStat : classStat.methodStats.values())
				if (methodStat.count > 0)
					methodStats.add(methodStat);

		if (sortBy != null)
			Collections.sort(methodStats, sortBy.comparator);

		final List<String> lines = new ArrayList<>();

		lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		lines.add("<entries>");
		lines.add("\t<!-- This XML contains statistics about execution times. -->");
		lines.add("\t<!-- Submitted results will help the developers to optimize the server. -->");

		final String[][] values = new String[SortBy.VALUES.length][methodStats.size()];
		final int[] maxLength = new int[SortBy.VALUES.length];

		for (int i = 0; i < SortBy.VALUES.length; i++) {
			final SortBy sort = SortBy.VALUES[i];

			for (int k = 0; k < methodStats.size(); k++) {
				@SuppressWarnings("rawtypes")
				final Comparable c = sort.getComparableValueOf(methodStats.get(k));

				final String value;

				if (c instanceof Number)
					value = NumberFormat.getInstance(Locale.ENGLISH).format(((Number) c).longValue());
				else
					value = String.valueOf(c);

				values[i][k] = value;

				maxLength[i] = Math.max(maxLength[i], value.length());
			}
		}

		for (int k = 0; k < methodStats.size(); k++) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t<entry ");

			EnumSet<SortBy> set = EnumSet.allOf(SortBy.class);

			if (sortBy != null) {
				switch (sortBy) {
					case NAME:
					case METHOD:
						appendAttribute(sb, SortBy.NAME, values[SortBy.NAME.ordinal()][k], maxLength[SortBy.NAME.ordinal()]);
						set.remove(SortBy.NAME);

						appendAttribute(sb, SortBy.METHOD, values[SortBy.METHOD.ordinal()][k], maxLength[SortBy.METHOD.ordinal()]);
						set.remove(SortBy.METHOD);
						break;
					default:
						appendAttribute(sb, sortBy, values[sortBy.ordinal()][k], maxLength[sortBy.ordinal()]);
						set.remove(sortBy);
						break;
				}
			}

			for (SortBy sort : SortBy.VALUES)
				if (set.contains(sort))
					appendAttribute(sb, sort, values[sort.ordinal()][k], maxLength[sort.ordinal()]);

			sb.append("/>");

			lines.add(sb.toString());
		}

		lines.add("</entries>");

		try {
			Path statsFolder = Paths.get("./log/stats");
			Files.createDirectories(statsFolder);
			try (PrintStream ps = new PrintStream(statsFolder + "/MethodStats.log")) {
				for (String line : lines)
					ps.println(line);
			}
		} catch (Exception e) {
			log.warn("", e);
		}
	}

	private static void appendAttribute(StringBuilder sb, SortBy sortBy, String value, int fillTo) {
		sb.append(sortBy.xmlAttributeName);
		sb.append("=");

		if (sortBy != SortBy.NAME && sortBy != SortBy.METHOD)
			for (int i = value.length(); i < fillTo; i++)
				sb.append(" ");

		sb.append("\"");
		sb.append(value);
		sb.append("\" ");

		if (sortBy == SortBy.NAME || sortBy == SortBy.METHOD)
			for (int i = value.length(); i < fillTo; i++)
				sb.append(" ");
	}
}
