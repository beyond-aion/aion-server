package com.aionemu.gameserver.model.raid;

import com.aionemu.gameserver.model.templates.raid.RaidTemplate;


/**
 * @author Alcapwnd
 */
public class RaidLocation {

    protected RaidTemplate template;

    public RaidLocation() {
    }

    public RaidLocation(RaidTemplate template) {
        this.template = template;
    }

    public int getId() {
        return template.getId();
    }

    public int getWorldId() {
        return template.getWorldId();
    }
    
    public float getX() {
    	return template.getX();
    }
    
    public float getY() {
    	return template.getY();
    }
    
    public float getZ() {
    	return template.getZ();
    }
    
    public int getH() {
    	return template.getH();
    }
}