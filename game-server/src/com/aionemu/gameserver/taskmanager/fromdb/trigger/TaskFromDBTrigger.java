package com.aionemu.gameserver.taskmanager.fromdb.trigger;

import com.aionemu.gameserver.taskmanager.fromdb.handler.TaskFromDBHandler;

/**
 *
 * @author nrg
 */
public abstract class TaskFromDBTrigger implements Runnable {
    
    protected TaskFromDBHandler handlerToTrigger;
    protected String[] params = {""}; 
    
    public int getTaskId() {
        return handlerToTrigger.getTaskId();
    }
    
    public TaskFromDBHandler getHandlerToTrigger() {
        return handlerToTrigger;
    }

    public void setHandlerToTrigger(TaskFromDBHandler handlerToTrigger) {
        this.handlerToTrigger = handlerToTrigger;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }  
    
    public final boolean isValid() {
		if (handlerToTrigger == null)
			return false;
        return this.isValidTrigger() && handlerToTrigger.isValid();
    }
    
    public abstract boolean isValidTrigger();
    
    public abstract void initTrigger();
    
    @Override
    public void run() {
        handlerToTrigger.trigger();
    }
}
