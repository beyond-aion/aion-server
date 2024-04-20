package com.aionemu.gameserver.cache;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HTMLConfig;

/**
 * @author Layane, nbali, savormix, hex1r0, lord_rex
 */
public final class HTMLCache {

	private static final Logger log = LoggerFactory.getLogger(HTMLCache.class);

	private static final FileFilter HTML_FILTER = new FileFilter() {

		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().endsWith(".xhtml");
		}
	};

	private static final File HTML_ROOT = new File(HTMLConfig.HTML_ROOT);

	private static final class SingletonHolder {

		private static final HTMLCache INSTANCE = new HTMLCache();
	}

	public static HTMLCache getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private Map<String, String> cache = new HashMap<>();

	private int loadedFiles;
	private int size;

	private HTMLCache() {
		reload(false);
	}

	@SuppressWarnings("unchecked")
	public synchronized void reload(boolean deleteCacheFile) {
		cache.clear();
		loadedFiles = 0;
		size = 0;

		final File cacheFile = getCacheFile();

		if (deleteCacheFile && cacheFile.exists()) {
			log.info("Cache[HTML]: Deleting cache file... OK.");

			cacheFile.delete();
		}

		log.info("Cache[HTML]: Caching started... OK.");

		if (cacheFile.exists()) {
			log.info("Cache[HTML]: Using cache file... OK.");

			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getCacheFile())))) {
				cache = (Map<String, String>) ois.readObject();

				for (String html : cache.values()) {
					loadedFiles++;
					size += html.length();
				}
			} catch (Exception e) {
				log.warn("", e);

				reload(true);
				return;
			}
		} else {
			parseDir(HTML_ROOT);
		}

		log.info(String.valueOf(this));

		if (cacheFile.exists()) {
			log.info("Cache[HTML]: Compaction skipped!");
		} else {
			log.info("Cache[HTML]: Compacting htmls... OK.");

			final StringBuilder sb = new StringBuilder(8192);

			for (Entry<String, String> entry : cache.entrySet()) {
				try {
					final String oldHtml = entry.getValue();
					final String newHtml = compactHtml(sb, oldHtml);

					size -= oldHtml.length();
					size += newHtml.length();

					entry.setValue(newHtml);
				} catch (RuntimeException e) {
					log.warn("Cache[HTML]: Error during compaction of " + entry.getKey(), e);
				}
			}

			log.info(String.valueOf(this));
		}

		if (!cacheFile.exists()) {
			log.info("Cache[HTML]: Creating cache file... OK.");

			try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile())))) {
				oos.writeObject(cache);
			} catch (IOException e) {
				log.warn("", e);
			}
		}
	}

	private File getCacheFile() {
		return new File(HTMLConfig.HTML_CACHE_FILE);
	}

	private static final String[] TAGS_TO_COMPACT;

	static {
		// TODO: is there any other tag that should be replaced?
		final String[] tagsToCompact = { "html", "title", "body", "br", "br1", "p", "table", "tr", "td" };

		final List<String> list = new ArrayList<>();

		for (String tag : tagsToCompact) {
			list.add("<" + tag + ">");
			list.add("</" + tag + ">");
			list.add("<" + tag + "/>");
			list.add("<" + tag + " />");
		}

		final List<String> list2 = new ArrayList<>();

		for (String tag : list) {
			list2.add(tag);
			list2.add(tag + " ");
			list2.add(" " + tag);
		}

		TAGS_TO_COMPACT = list2.toArray(new String[list.size()]);
	}

	private String compactHtml(StringBuilder sb, String html) {
		sb.setLength(0);
		sb.append(html);

		for (int i = 0; i < sb.length(); i++)
			if (Character.isWhitespace(sb.charAt(i)))
				sb.setCharAt(i, ' ');

		replaceAll(sb, "  ", " ");

		replaceAll(sb, "< ", "<");
		replaceAll(sb, " >", ">");

		for (int i = 0; i < TAGS_TO_COMPACT.length; i += 3) {
			replaceAll(sb, TAGS_TO_COMPACT[i + 1], TAGS_TO_COMPACT[i]);
			replaceAll(sb, TAGS_TO_COMPACT[i + 2], TAGS_TO_COMPACT[i]);
		}

		replaceAll(sb, "  ", " ");

		// String.trim() without additional garbage
		int fromIndex = 0;
		int toIndex = sb.length();

		while (fromIndex < toIndex && sb.charAt(fromIndex) == ' ')
			fromIndex++;

		while (fromIndex < toIndex && sb.charAt(toIndex - 1) == ' ')
			toIndex--;

		return sb.substring(fromIndex, toIndex);
	}

	private void replaceAll(StringBuilder sb, String pattern, String value) {
		for (int index = 0; (index = sb.indexOf(pattern, index)) != -1;)
			sb.replace(index, index + pattern.length(), value);
	}

	public void reloadPath(File f) {
		parseDir(f);

		log.info("Cache[HTML]: Reloaded specified path.");
	}

	public void parseDir(File dir) {
		for (File file : dir.listFiles(HTML_FILTER)) {
			if (!file.isDirectory())
				loadFile(file);
			else
				parseDir(file);
		}
	}

	public String loadFile(File file) {
		if (isLoadable(file)) {
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
				byte[] raw = new byte[bis.available()];
				bis.read(raw);

				String content = new String(raw, HTMLConfig.HTML_ENCODING);
				String relpath = getRelativePath(HTML_ROOT, file);

				size += content.length();

				String oldContent = cache.get(relpath);
				if (oldContent == null)
					loadedFiles++;
				else
					size -= oldContent.length();

				cache.put(relpath, content);

				return content;
			} catch (Exception e) {
				log.warn("Problem with htm file:", e);
			}
		}

		return null;
	}

	public String getHTML(String path) {
		return cache.get(path);
	}

	private boolean isLoadable(File file) {
		return file.exists() && !file.isDirectory() && HTML_FILTER.accept(file);
	}

	public boolean pathExists(String path) {
		return cache.containsKey(path);
	}

	@Override
	public String toString() {
		return "Cache[HTML]: " + String.format("%.3f", (float) size / 1024) + " kilobytes on " + loadedFiles + " file(s) loaded.";
	}

	public static String getRelativePath(File base, File file) {
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}
}
