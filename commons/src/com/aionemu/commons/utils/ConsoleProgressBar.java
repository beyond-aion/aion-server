package com.aionemu.commons.utils;

/**
 * @author Neon
 */
class ConsoleProgressBar {

	private static final int MAX_POSITION = ConsoleUtil.DEFAULT_CONSOLE_WIDTH - 12;

	private int position;
	private int currentProgress;
	private double maxProgress; // double for division purposes

	/**
	 * Prevent instantiation
	 */
	private ConsoleProgressBar() {
	}

	/**
	 * Initializes and prints a progress bar with the given capacity to the console.<br>
	 * Only one progress bar can be used at a time.
	 * 
	 * @param maxProgress
	 *          The capacity of the progress
	 */
	protected void init(int maxProgress) {
		this.position = 0;
		this.currentProgress = 0;
		this.maxProgress = maxProgress;
		StringBuilder bar = new StringBuilder(" 0% [");
		for (int i = 0; i < (maxProgress < MAX_POSITION ? maxProgress : MAX_POSITION); i++)
			bar.append("·");
		bar.append("] 100%\r 0% [");
		System.out.print(bar);
	}

	/**
	 * Prints the progress to the progress bar.<br>
	 * Note that {@link #init(int maxProgress)} must be called prior to calling this method for the first time.
	 */
	protected void increaseAndPrintProgress() {
		if (maxProgress <= 0)
			return;

		double pos = ((++currentProgress / maxProgress) * (maxProgress < MAX_POSITION ? maxProgress : MAX_POSITION));

		if (position < pos || currentProgress == maxProgress) {
			for (; position < pos; position++)
				System.out.print("#");
			if (currentProgress == maxProgress)
				System.out.println("] 100%");
		} else if (currentProgress < maxProgress && maxProgress > MAX_POSITION * 2) {
			switch (currentProgress % 3) {
				case 0:
					System.out.print("/\b");
					break;
				case 1:
					System.out.print("-\b");
					break;
				case 2:
					System.out.print("\\\b");
					break;
			}
		}
	}

	private static final class SingletonHolder {

		private static final ConsoleProgressBar INSTANCE = new ConsoleProgressBar();
	}

	protected static ConsoleProgressBar getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
