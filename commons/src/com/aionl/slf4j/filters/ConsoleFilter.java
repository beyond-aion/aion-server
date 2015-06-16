package com.aionl.slf4j.filters;

import ch.qos.logback.classic.spi.ILoggingEvent; 
import ch.qos.logback.core.filter.Filter; 
import ch.qos.logback.core.spi.FilterReply; 

/**
 * @author zhkchi
 *
 */
public class ConsoleFilter extends Filter<ILoggingEvent>{

	@Override 
  public FilterReply decide(ILoggingEvent event) {     
    if (event.getMessage().startsWith("[MESSAGE]") || event.getMessage().startsWith("[ITEM]")
    	|| event.getMessage().startsWith("[ADMIN COMMAND]") || event.getMessage().startsWith("[AUDIT]")) { 
      return FilterReply.DENY;
    } else { 
      return FilterReply.ACCEPT;
    } 
  }
}
