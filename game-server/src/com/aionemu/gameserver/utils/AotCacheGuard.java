package com.aionemu.gameserver.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32C;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;

/**
 * The JVM only checks the size and modification time of the jars an AOT cache was created for. All jars of a build have the same modification
 * time, so the cache of an older build could still be used and run its classes instead of the new ones. This stores a fingerprint of the jar
 * contents, the JVM and its options next to the cache and restarts the server if they changed, after marking the cache as outdated for the start
 * script.
 */
public final class AotCacheGuard {

	private static final Logger log = LoggerFactory.getLogger(AotCacheGuard.class);
	private static final String CACHE_OPTION = "-XX:AOTCache=";
	private static final String OUTPUT_OPTION = "-XX:AOTCacheOutput=";

	private AotCacheGuard() {
	}

	public static void check() {
		List<String> jvmOptions = ManagementFactory.getRuntimeMXBean().getInputArguments();
		String cache = option(jvmOptions, CACHE_OPTION);
		String output = option(jvmOptions, OUTPUT_OPTION);
		try {
			if (output != null) {
				Path keyFile = Path.of(output + ".key");
				Files.createDirectories(keyFile.toAbsolutePath().getParent());
				Files.writeString(keyFile, fingerprint(jvmOptions));
			} else if (cache != null && Files.isRegularFile(Path.of(cache))) {
				Path keyFile = Path.of(cache + ".key");
				String key = Files.isRegularFile(keyFile) ? Files.readString(keyFile) : null;
				if (!fingerprint(jvmOptions).equals(key)) {
					Files.writeString(Path.of(cache + ".outdated"), "");
					log.warn("AOT cache {} was created for other jars or JVM options, restarting to recreate it", cache);
					System.exit(ExitCode.RESTART);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String option(List<String> jvmOptions, String prefix) {
		return jvmOptions.stream().filter(o -> o.startsWith(prefix)).map(o -> o.substring(prefix.length())).findFirst().orElse(null);
	}

	private static String fingerprint(List<String> jvmOptions) throws IOException {
		CRC32C crc = new CRC32C();
		byte[] buffer = new byte[1 << 16];
		// all entries in their order, since the JVM still uses archived classes if a directory with newer ones is put in front of the jars
		for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
			crc.update(entry.getBytes());
			Path path = Path.of(entry);
			if (!Files.isRegularFile(path))
				continue;
			try (InputStream in = Files.newInputStream(path)) {
				for (int read; (read = in.read(buffer)) > 0;)
					crc.update(buffer, 0, read);
			}
		}
		StringBuilder options = new StringBuilder(System.getProperty("java.vm.version"));
		for (String option : jvmOptions) {
			if (!option.startsWith(CACHE_OPTION) && !option.startsWith(OUTPUT_OPTION))
				options.append(' ').append(option);
		}
		return Long.toHexString(crc.getValue()) + ' ' + options;
	}
}
