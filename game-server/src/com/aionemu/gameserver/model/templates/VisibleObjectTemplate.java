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
	 * @return the cylinder of this object, or {@link BoundRadius#ZERO} if it has no size of its own
	 */
	public BoundRadius getBoundRadius() {
		return BoundRadius.ZERO;
	}

}
