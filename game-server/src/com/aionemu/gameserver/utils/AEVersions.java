package com.aionemu.gameserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.AEInfos;
import com.aionemu.commons.versionning.Version;
import com.aionemu.gameserver.GameServer;

/**
 * @author lord_rex
 */
public class AEVersions {

	private static final Logger log = LoggerFactory.getLogger(AEVersions.class);
	private static final Version commons = new Version(AEInfos.class);
	private static final Version gameserver = new Version(GameServer.class);

	private static String getRevisionInfo(Version version) {
		return String.format("%-6s", version.getRevision());
	}

	private static String getBranchInfo(Version version) {
		return String.format("%-6s", version.getBranch());
	}

	private static String getBranchCommitTimeInfo(Version version) {
		return String.format("%-6s", version.getCommitTime());
	}

	private static String getDateInfo(Version version) {
		return String.format("[ %4s ]", version.getDate());
	}

	public static String[] getFullVersionInfo() {
		return new String[] { "Commons Revision: " + getRevisionInfo(commons),
			"Commons Build Date: " + getDateInfo(commons), "GS Revision: " + getRevisionInfo(gameserver),
			"GS Branch: " + getBranchInfo(gameserver), "GS Branch Commit Date: " + getBranchCommitTimeInfo(gameserver),
			"GS Build Date: " + getDateInfo(gameserver), "..................................................",
			".................................................." };
	}

	public static void printFullVersionInfo() {
		for (String line : getFullVersionInfo())
			log.info(line);
	}
}
