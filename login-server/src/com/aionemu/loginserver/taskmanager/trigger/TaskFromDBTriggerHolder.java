package com.aionemu.loginserver.taskmanager.trigger;

import com.aionemu.loginserver.taskmanager.trigger.implementations.AfterRestartTrigger;
import com.aionemu.loginserver.taskmanager.trigger.implementations.FixedInTimeTrigger;

/**
 *
 * @author nrg
 */
public enum TaskFromDBTriggerHolder {
    FIXED_IN_TIME(FixedInTimeTrigger.class),
	AFTER_RESTART(AfterRestartTrigger.class);
    
    private Class<? extends TaskFromDBTrigger> triggerClass;
    
    private TaskFromDBTriggerHolder(Class<? extends TaskFromDBTrigger> triggerClass) {
        this.triggerClass = triggerClass;
    }
    
    public Class<? extends TaskFromDBTrigger> getTriggerClass() {
        return triggerClass;
    }
}
