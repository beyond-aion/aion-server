package com.aionemu.commons.scripting;

import java.io.File;
import java.util.*;

import com.aionemu.commons.scripting.classlistener.ClassListener;
import com.aionemu.commons.scripting.impl.ScriptContextImpl;

/**
 * Class that represents managers of script contexts. It loads, reloads and unload script contexts. In the future it may be extended to support
 * programmatic manipulation of contexts, but for now it's not needed. <br />
 * Example:
 * 
 * <pre>
 *      ScriptManager sm = new ScriptManager();
 *      sm.load(new File(&quot;st/javaSourceRootDirectory&quot;));
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
	 * Collection of script contexts
	 */
	private Set<ScriptContext> contexts = new HashSet<>();

	/**
	 * Global ClassListener instance. Automatically assigned for each new context. Fires after each successful compilation.
	 */
	private ClassListener globalClassListener;

	/**
	 * Loads .java files from all given directories recursively.
	 */
	public synchronized void load(File... sourceFileRootDirectories) {
		ScriptContext context = createUniqueContext(sourceFileRootDirectories);
		contexts.add(context);
		context.init();
	}

	private ScriptContext createUniqueContext(File... sourceFileDirectories) {
		ScriptContext context = new ScriptContextImpl(sourceFileDirectories);
		if (contexts.contains(context))
			throw new IllegalArgumentException("Script context already exists for these directories: " + Arrays.toString(sourceFileDirectories));

		if (globalClassListener != null)
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
