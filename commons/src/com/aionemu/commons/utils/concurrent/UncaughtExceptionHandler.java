package com.aionemu.commons.utils.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptCompilerCache;
import com.aionemu.commons.utils.ExitCode;

/**
 * @author -Nemesiss-
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Critical Error - Thread [" + t.getName() + "] terminated abnormally:", e);
		if (e instanceof OutOfMemoryError) {
			log.error("Trying to exit gracefully with ExitCode.RESTART..."); // we shouldn't even try to regain memory at this point
			Thread.startVirtualThread(() -> System.exit(ExitCode.RESTART)); // async since System.exit indefinitely blocks the calling thread
		} else if (e instanceof LinkageError && ScriptCompilerCache.contains(e.getStackTrace()[0].getClassName())) {
			log.error("Cached class " + e.getStackTrace()[0].getClassName()
				+ " is not binary compatible to a class it imports, because of some signature change in the imported class. Please delete "
				+ ScriptCompilerCache.CACHE_DIR + " and restart");
		}
		if (isMainThread(t) && anyExitBlockingThread(t)) // crashed main thread should exit immediately
			Thread.startVirtualThread(() -> System.exit(ExitCode.ERROR)); // async since System.exit indefinitely blocks the calling thread
	}

	private boolean isMainThread(Thread t) {
		return t.threadId() == 1;
	}

	private boolean anyExitBlockingThread(Thread ignoredThread) {
		return Thread.getAllStackTraces().keySet().stream().anyMatch(lt -> lt != ignoredThread && !lt.isDaemon());
	}
}
