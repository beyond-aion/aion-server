package com.aionemu.commons.utils.info;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lord_rex, Neon
 */
public class SystemInfo {

	private static final Logger log = LoggerFactory.getLogger(SystemInfo.class);

	public static String[] getMemoryInfo() {
		int mbDivisor = 1024 * 1024;
		double max = Runtime.getRuntime().maxMemory() / mbDivisor;
		double allocated = Runtime.getRuntime().totalMemory() / mbDivisor;
		double free = Runtime.getRuntime().freeMemory() / mbDivisor;
		double used = allocated - free;
		DecimalFormat mbFmt = new DecimalFormat("#,##0 'MiB'");
		DecimalFormat percentFmt = new DecimalFormat("(0 %)");

		return new String[] {
			"Max. memory allowed: " + String.format("%9s", mbFmt.format(max)),
			"├ Allocated memory:  " + String.format("%9s", mbFmt.format(allocated)) + String.format("%8s", percentFmt.format(allocated / max)),
			"└ Used memory:       " + String.format("%9s", mbFmt.format(used)) + String.format("%8s", percentFmt.format(used / max)),
		};
	}

	public static String[] getSystemInfo() {
		int availableCPUs = Runtime.getRuntime().availableProcessors();
		String totalCPUs = System.getenv("NUMBER_OF_PROCESSORS");
		totalCPUs = (totalCPUs == null || totalCPUs.equals(availableCPUs + "") ? "" : String.format("%12s", " (of " + totalCPUs + ')'));
		return new String[] {
			"OS:  " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ')' + " version " + System.getProperty("os.version"),
			"JVM: " + System.getProperty("java.vm.name") + " version " + Runtime.version() + " (" + System.getProperty("java.version.date") + ')',
			"JVM available CPUs: " + String.format("%6s", availableCPUs) + totalCPUs
		};
	}

	public static void logAll() {
		for (String[] info : Arrays.asList(getSystemInfo(), getMemoryInfo())) {
			for (String line : info)
				log.info(line);
		}
	}
}
