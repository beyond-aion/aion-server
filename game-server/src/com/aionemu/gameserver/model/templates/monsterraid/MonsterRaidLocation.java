package com.aionemu.gameserver.model.templates.monsterraid;

import java.util.List;


/**
 * @author Whoop
 *
 */
public class MonsterRaidLocation {

	protected MonsterRaidTemplate template;
	
	public MonsterRaidLocation() {
		
	}
	
	public MonsterRaidLocation(MonsterRaidTemplate template) {
		this.template = template;
	}
	
	public int getLocationId() {
		return template.getLocationId();
	}
	
	public int getWorldId() {
		return template.getWorldId();
	}
	
	public List<Integer> getNpcIds() {
		return template.getNpcIds();
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
	
	public byte getH(){
		return template.getH();
	}
}
