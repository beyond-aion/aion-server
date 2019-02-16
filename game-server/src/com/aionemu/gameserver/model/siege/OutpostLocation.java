package com.aionemu.gameserver.model.siege;

import java.util.List;

import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;

/**
 * These bosses only appear when a faction conquers the balaurea fortresses of their enemy map.
 * If Elyos conquer Gelkmaros' fortresses Enraged Mastarius will appear on Ancient City of Marayas.
 * If Asmodians conquer Inggison's fortresses Enraged Veille will appear on Inggison Outpost.
 * He/She will stay for about 2 hours after that he/she disappears and re-spawns after the end of the next siege if conditions
 * are still met.
 * 
 * @author Source
 */
public class OutpostLocation extends SiegeLocation {

	public OutpostLocation(SiegeLocationTemplate template) {
		super(template);
	}

	@Override
	public int getNextState() {
		return isVulnerable() ? STATE_INVULNERABLE : STATE_VULNERABLE;
	}

	/**
	 * @return Fortresses that must be captured to own this outpost
	 */
	public List<Integer> getFortressDependency() {
		return getTemplate().getFortressDependency();
	}

	/**
	 * Shouldn't be necessary anymore, but re-check packets first before removing this.
	 * Silentera entrances do not depend on fortresses since 4.x.
	 */
	public boolean isSilenteraAllowed() {
		return true;
	}
}
