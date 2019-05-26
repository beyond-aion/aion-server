package com.aionemu.commons.scripting.scriptmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptContext;
import com.aionemu.commons.scripting.ScriptContextFactory;
import com.aionemu.commons.scripting.classlistener.ClassListener;
import com.aionemu.commons.utils.xml.JAXBUtil;

/**
 * Class that represents managers of script contexts. It loads, reloads and unload script contexts. In the future it may be extended to support
 * programmatic manipulation of contexts, but for now it's not needed. <br />
 * Example:
 * 
 * <pre>
 *      ScriptManager sm = new ScriptManager();
 *      sm.load(new File(&quot;st/contexts.xml&quot;));
 *      ...
 *      sm.shutdown();
 * </pre>
 * 
 * <br>
 * 
 * @author SoulKeeper, Aquanox
 */
public class ScriptManager {

	/**
	 * Logger for script context
	 */
	private static final Logger log = LoggerFactory.getLogger(ScriptManager.class);

	/**
	 * Collection of script contexts
	 */
	private Set<ScriptContext> contexts = new HashSet<>();

	/**
	 * Global ClassListener instance. Automatically assigned for each new context. Fires after each successful compilation.
	 */
	private ClassListener globalClassListener;

	/**
	 * Loads script contexts from descriptor
	 * 
	 * @param scriptDescriptor
	 *          xml file that describes contexts
	 */
	public synchronized void load(File scriptDescriptor) {
		ScriptList list = JAXBUtil.deserialize(scriptDescriptor, ScriptList.class);

		for (ScriptInfo si : list.getScriptInfos()) {
			ScriptContext context = createContext(si, null);
			contexts.add(context);
			context.init();
		}
	}

	/**
	 * Convenient method that is used to load all script files and libraries from specific directory.<br>
	 * Descriptor is not required.<br>
	 * <br>
	 * <b>If you wish complex context hierarchy - you will have to use context descriptors</b> <br>
	 * <br>
	 * .java files are treated as sources.<br>
	 * .jar files are treated as libraries.<br>
	 * Both .java and .jar files will be loaded recursively
	 *
	 * @param directory
	 *          - directory with .java and .jar files
	 * @throws IOException
	 *           if failed to read the directory contents
	 */
	public synchronized void loadDirectory(File directory) throws IOException {
		List<File> libraries = Files.walk(directory.toPath(), Integer.MAX_VALUE).filter(Files::isRegularFile)
			.filter(path -> path.toString().toLowerCase().endsWith(".jar")).map(Path::toFile).collect(Collectors.toList());
		try {
			loadDirectory(directory.getAbsolutePath(), libraries);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load script context from directory " + directory.getAbsolutePath(), e);
		}
	}

	/**
	 * Load scripts directly from<br>
	 * <br>
	 * <b>If you wish complex context hierarchy - you will have to use context descriptors</b> <br>
	 * <br>
	 * 
	 * @param directory
	 *          - directory with source files
	 * @param libraries
	 *          - collection with libraries to load
	 */
	public synchronized void loadDirectory(String directory, List<File> libraries) {
		ScriptInfo si = new ScriptInfo();
		si.setRoot(directory);
		si.setScriptInfos(Collections.emptyList());
		si.setLibraries(libraries);

		ScriptContext sc = createContext(si, null);
		contexts.add(sc);
		sc.init();
	}

	/**
	 * Creates new context and checks to not produce copies
	 * 
	 * @param si
	 *          script context descriptor
	 * @param parent
	 *          parent script context
	 * @return created script context
	 */
	protected ScriptContext createContext(ScriptInfo si, ScriptContext parent) {
		ScriptContext context = ScriptContextFactory.getScriptContext(si.getRoot(), parent);
		context.setLibraries(si.getLibraries());

		if (parent == null && contexts.contains(context)) {
			log.warn("Double root script context definition: " + si.getRoot());
			return null;
		}

		if (si.getScriptInfos() != null && !si.getScriptInfos().isEmpty()) {
			for (ScriptInfo child : si.getScriptInfos()) {
				createContext(child, context);
			}
		}

		if (parent == null && globalClassListener != null)
			context.setClassListener(globalClassListener);

		return context;
	}

	/**
	 * Initializes shutdown on all contexts
	 */
	public synchronized void shutdown() {
		for (ScriptContext context : contexts) {
			context.shutdown();
		}

		contexts.clear();
	}

	/**
	 * Reloads all contexts
	 */
	public synchronized void reload() {
		for (ScriptContext context : contexts) {
			reloadContext(context);
		}
	}

	/**
	 * Reloads specified context.
	 * 
	 * @param ctx
	 *          Script context instance.
	 */
	public void reloadContext(ScriptContext ctx) {
		ctx.reload();
	}

	/**
	 * Returns unmodifiable set with script contexts
	 * 
	 * @return unmodifiable set of script contexts
	 */
	public synchronized Collection<ScriptContext> getScriptContexts() {
		return Collections.unmodifiableSet(contexts);
	}

	/**
	 * Set Global class listener instance.
	 * 
	 * @param instance
	 *          listener instance.
	 */
	public void setGlobalClassListener(ClassListener instance) {
		this.globalClassListener = instance;
	}
}
