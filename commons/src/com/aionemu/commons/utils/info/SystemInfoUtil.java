package com.aionemu.commons.utils.info;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ConsoleUtil;

/**
 * This class is for get/log system informations.
 * 
 * @author lord_rex
 * @modified Neon
 */
public class SystemInfoUtil {

	private static final Logger log = LoggerFactory.getLogger(SystemInfoUtil.class);

	public static String[] getMemoryInfo() {
		int mbDivisor = 1024 * 1024;
		double max = Runtime.getRuntime().maxMemory() / mbDivisor; // the upper limit the jvm can use
		double allocated = Runtime.getRuntime().totalMemory() / mbDivisor; // the size of the current allocation pool
		double free = Runtime.getRuntime().freeMemory() / mbDivisor; // the unused memory in the allocation pool
		double used = allocated - free; // really used memory
		double totalFree = max - used; // non-used + non-allocated memory
		DecimalFormat mbFmt = new DecimalFormat("#,##0 'MB'");
		DecimalFormat percentFmt = new DecimalFormat(" (0.#%)");

		return new String[] {
			"Current Memory Statistics",
			" Allowed memory:\t" + mbFmt.format(max),
			" ├ Allocated:\t\t" + mbFmt.format(allocated) + percentFmt.format(allocated / max),
			" │ ├ Used:\t\t" + mbFmt.format(used) + percentFmt.format(used / allocated),
			" │ └ Free:\t\t" + mbFmt.format(free) + percentFmt.format(free / allocated),
			" └ Total free:\t\t" + mbFmt.format(totalFree) + percentFmt.format(totalFree / max)
		};
	}

	public static String[] getOsInfo() {
		return new String[] {
			"Operating System Information",
			" OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"),
			" OS Arch: " + System.getProperty("os.arch")
		};
	}

	public static String[] getJreInfo() {
		return new String[] {
			"Java Runtime Environment Information",
			" Java Runtime Name: " + System.getProperty("java.runtime.name"),
			" Java Version: " + System.getProperty("java.version"),
			" Java Class Version: " + System.getProperty("java.class.version"),
			" JRE Path (java.home): " + System.getProperty("java.home"),
			" JDK Path (JAVA_HOME): " + System.getenv("JAVA_HOME")
		};
	}

	public static String[] getJvmInfo() {
		return new String[] {
			"Java Virtual Machine Information",
			" JVM Name: " + System.getProperty("java.vm.name"),
			" JVM version: " + System.getProperty("java.vm.version"),
			" JVM Vendor: " + System.getProperty("java.vm.vendor"),
			" JVM Info: " + System.getProperty("java.vm.info")
		};
	}

	public static String[] getJvmCpuInfo() {
		return new String[] {
			"Virtual Machines Processor Information",
			" Available CPU(s): " + Runtime.getRuntime().availableProcessors(),
			" Processor Identifier: " + System.getenv("PROCESSOR_IDENTIFIER")
		};
	}

	public static List<String[]> getAllInfo() {
		return Arrays.asList(getOsInfo(), getJreInfo(), getJvmInfo(), getJvmCpuInfo(), getMemoryInfo());
	}

	public static void printAllInfo() {
		final String SEPARATOR = ConsoleUtil.getSeparatorForLogger();

		for (String[] info : getAllInfo()) {
			for (String line : info)
				log.info(line);
			log.info(SEPARATOR);
		}
	}
}
