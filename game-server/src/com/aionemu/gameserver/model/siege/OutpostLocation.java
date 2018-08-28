package com.aionemu.gameserver.model.siege;

import java.util.List;

import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.services.SiegeService;

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
	 * @return Outpost Location Race
	 */
	public SiegeRace getLocationRace() {
		// TODO Should be configured from datapack
		switch (getLocationId()) {
			case 3111:
				return SiegeRace.ASMODIANS;
			case 2111:
				return SiegeRace.ELYOS;
			default:
				throw new RuntimeException("Please move this to datapack");
		}
	}

	/**
	 * @return Fortresses that must be captured to own this outpost
	 */
	public List<Integer> getFortressDependency() {
		return getTemplate().getFortressDependency();
	}

	public boolean isSiegeAllowed() {
		return getLocationRace() == getRace();
	}

	public boolean isSilenteraAllowed() {
		return !isSiegeAllowed() && !getRace().equals(SiegeRace.BALAUR);
	}

	public boolean areFortressesOccupiedByAnotherFaction() {
		for (Integer fortressId : getFortressDependency()) {
			SiegeRace fortressSiegeRace = SiegeService.getInstance().getFortresses().get(fortressId).getRace();
			if (fortressSiegeRace == getLocationRace())
				return false;
		}
		return true;
	}
}
