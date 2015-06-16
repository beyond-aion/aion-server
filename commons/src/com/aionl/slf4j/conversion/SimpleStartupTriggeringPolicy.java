package com.aionl.slf4j.conversion;

import java.io.File;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;

/** 
 * SimpleStartupTriggeringPolicy triggers a rollover once at startup only. This 
 * is useful for preserving older logfiles during development. 
 * 
 * @author Rick Beton 
 */ 
public final class SimpleStartupTriggeringPolicy<E> extends TriggeringPolicyBase<E> { 

    private boolean fired = false; 


    public SimpleStartupTriggeringPolicy () { 
        // does nothing 
    } 

    @Override
	public boolean isTriggeringEvent( final File activeFile, final E event ) { 
        final boolean result = !fired && (activeFile.length() > 0); 
        fired = true; 
        if (result) { 
            addInfo( "Triggering rollover for " + activeFile ); 
        } 
        return result; 
    } 
}