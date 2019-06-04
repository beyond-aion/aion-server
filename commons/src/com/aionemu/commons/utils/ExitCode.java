package com.aionemu.commons.utils;

/**
 * Class that contains exit codes for server
 * 
 * @author SoulKeeper
 */
public final class ExitCode {

	/**
	 * Private constructor to avoid instantiation
	 */
	private ExitCode() {
	}

	/**
	 * Indicates that server successfully finished it's work
	 */
	public static final int NORMAL = 0;

	/**
	 * Indicates that server successfully finished it's work and should be restarted
	 */
	public static final int RESTART = 2;

	/**
	 * Indicates that error happened in server and it's need to shutdown.<br>
	 * Shit happens :(
	 */
	public static final int ERROR = 1;
}
