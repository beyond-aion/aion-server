package com.aionemu.commons.scripting.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method marked as {@link OnClassUnload} will be called when there is a script reload or shutdown.<br>
 * Only static methods with no arguments can be marked with this annotation.<br>
 * This is only used if {@link com.aionemu.commons.scripting.ScriptContext#getClassListener()} returns instance of
 * {@link com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener} subclass.
 * 
 * @author SoulKeeper
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClassUnload {
}
