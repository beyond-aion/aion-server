package com.aionemu.gameserver.model.templates;

/**
 * @author ATracer
 */
public abstract class VisibleObjectTemplate {

	/**
	 * For Npcs it will return npcid from templates xml
	 * 
	 * @return id of object template
	 */
	public abstract int getTemplateId();

	/**
	 * For Npcs it will return name from templates xml
	 * 
	 * @return name of object
	 */
	public abstract String getName();

	/**
	 * Name id of object template
	 * 
	 * @return int
	 */
	public abstract int getNameId();

	/**
	 * @return
	 */
	public BoundRadius getBoundRadius() {
		return BoundRadius.DEFAULT;
	}

	/**
	 * @return default state
	 */
	public int getState() {
		return 0;
	}
}
