package com.aionemu.commons.utils.info;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupInfo {

	private static final Logger log = LoggerFactory.getLogger(StartupInfo.class);
	private static final String banner = """
		 ____   ______ __     __  ____   _   _  _____          __       _____   ____   _   _
		|  _ \\ |  ____|\\ \\   / / / __ \\ | \\ | ||  __ \\        /  \\     |_   _| / __ \\ | \\ | |
		| |_) || |__    \\ \\_/ / | |  | ||  \\| || |  | |      / /\\ \\      | |  | |  | ||  \\| |
		|  _ < |  __|    \\   /  | |  | || . ` || |  | |     / ____ \\     | |  | |  | || . ` |
		| |_) || |____    | |   | |__| || |\\  || |__| |    / /    \\ \\   _| |_ | |__| || |\\  |
		|____/ |______|   |_|    \\____/ |_| \\_||_____/    /_/      \\_\\ |_____| \\____/ |_| \\_|
		""";
	private static final int strapLineWidth = 85;
	private static long startNanos;

	private StartupInfo() {
	}

	/**
	 * Prints the logo, the server name and its revision to the console and logs the start. Call it right after initializing the logging.
	 */
	public static void logStarting(Class<?> serverClass, String serverName) {
		startNanos = System.nanoTime();
		String revision = new VersionInfo(serverClass).getRevision();
		String name = " :: " + serverName + " :: ";
		String version = revision == null ? "" : " (revision " + revision + ")";
		System.out.println();
		System.out.print(banner);
		String strapLine = "\033[32m" + name + "\033[0;39m";
		if (revision != null)
			strapLine += " ".repeat(Math.max(0, strapLineWidth - name.length() - version.length())) + "\033[2m" + version + "\033[0;39m";
		System.out.println(strapLine);
		System.out.println();
		log.info("Starting {} using Java {} with PID {} (started by {} in {})", serverName, System.getProperty("java.version"),
			ProcessHandle.current().pid(), System.getProperty("user.name"), System.getProperty("user.dir"));
	}

	public static void logStarted(String serverName) {
		log.info("Started {} in {} seconds (process running for {})", serverName, (System.nanoTime() - startNanos) / 1_000_000 / 1000.0,
			ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0);
	}
}
