package com.aionemu.commons.callbacks;

import java.lang.instrument.Instrumentation;

import com.aionemu.commons.callbacks.enhancer.GlobalCallbackEnhancer;
import com.aionemu.commons.callbacks.enhancer.ObjectCallbackEnhancer;

/**
 * This class is used as javaagent to do on-class-load transformations with objects whose methods are marked by
 * {@link com.aionemu.commons.callbacks.metadata.ObjectCallback} or {@link com.aionemu.commons.callbacks.metadata.GlobalCallback} annotation.<br>
 * Code is inserted dynamicly before method call and after method call.<br>
 * For implementation docs please reffer to: http://www.csg.is.titech.ac.jp/~chiba/javassist/tutorial/tutorial2.html<br>
 * <br>
 * Usage: java -javaagent:lib/ae_commons.jar
 *
 * @author SoulKeeper
 */
public class JavaAgentEnhancer {

	/**
	 * Premain method that registers this class as ClassFileTransformer
	 *
	 * @param args
	 *          arguments passed to javaagent, ignored
	 * @param instrumentation
	 *          Instrumentation object
	 */
	public static void premain(String args, Instrumentation instrumentation) {
		instrumentation.addTransformer(new ObjectCallbackEnhancer(), true);
		instrumentation.addTransformer(new GlobalCallbackEnhancer(), true);
	}
}
