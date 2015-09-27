package com.aionemu.commons.utils.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ConsoleUtil;

/**
 * @author Neon
 */
public class VersionInfoUtil {

	private static final Logger log = LoggerFactory.getLogger(VersionInfoUtil.class);

	/**
	 * Disallow instantiation
	 */
	private VersionInfoUtil() {
	}

	public static VersionInfo getVersionInfo(Class<?> c) {
		return new VersionInfo(c);
	}

	public static VersionInfo getCommonsVersionInfo() {
		return new VersionInfo(VersionInfoUtil.class);
	}

	public static void printAllInfo(Class<?> c) {
		printAllInfo(c, true);
	}

	public static void printAllInfo(Class<?> c, boolean includeCommonsInfo) {
		printInfo(getVersionInfo(c), includeCommonsInfo);
	}

	public static void printAllCommonsInfo() {
		printInfo(getCommonsVersionInfo(), false);
	}

	private static void printInfo(VersionInfo v, boolean includeCommonsInfo) {
		if (includeCommonsInfo) {
			VersionInfo commonsInfo = getCommonsVersionInfo();
			if (!v.getSourceName().equals(commonsInfo.getSourceName()))
				for (String line : commonsInfo.getAllInfo())
					log.info(line);
		}

		for (String line : v.getAllInfo())
			log.info(line);

		log.info(ConsoleUtil.getSeparatorForLogger());
	}
}
