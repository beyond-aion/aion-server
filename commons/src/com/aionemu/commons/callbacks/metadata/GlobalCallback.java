package com.aionemu.commons.callbacks.metadata;

import com.aionemu.commons.callbacks.Callback;

import java.lang.annotation.*;

/**
 * Annotation that is used to mark enhanceable methods or classes.<br>
 * <b>Abstract and native methods are not allowed</b>
 *
 * @author SoulKeeper
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("rawtypes")
public @interface GlobalCallback {

    /**
     * Returns callback class that will be used as listener
     *
     * @return callback class that will be used as listener
     */
    Class<? extends Callback> value();
}
