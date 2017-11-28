package com.aionemu.gameserver.model.templates;

/**
 * @author ATracer
 */
public abstract class VisibleObjectTemplate implements L10n {

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
	 * @return
	 */
	public BoundRadius getBoundRadius() {
		return BoundRadius.DEFAULT;
	}

}
